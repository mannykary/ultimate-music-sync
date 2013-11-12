package com.mannykary.ultimatemusicsync;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.mannykary.ultimatemusicsync.Track;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

public class SearchActivity extends ActionBarActivity {

	EditText searchArtistBox, searchTrackBox;
	String[] listViewText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    searchArtistBox = (EditText) findViewById(R.id.searchBoxArtist);
	    searchTrackBox = (EditText) findViewById(R.id.searchBoxTrack);
	    
	    //ListView lv = (ListView) findViewById(R.id.searchResults);
	}
	
	public void clearActivity(View view) {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	public void getSearchBoxText(View view) {

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String qArtist = searchArtistBox.getText().toString().trim().replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			String qTrack = searchTrackBox.getText().toString().trim().replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			
			String qURL = "http://ws.audioscrobbler.com/2.0/?method=track.search&artist=" 
							+ qArtist + "&track=" + qTrack + "&api_key=" 
							+ getString(R.string.lastfm_key);
			
			Log.i(SearchActivity.class.getName(), qURL);
			
			new DownloadXmlTask().execute(qURL);
			
			
		} else {
			// TODO Add error handling when there is no network connection.
			Log.i(SearchActivity.class.getName(), "No network connection!");
		}
	}
		
	
 // Implementation of AsyncTask used to download XML feed from last.fm
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
            	return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
                
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
         
            }
        }
        
        @Override
        protected void onPostExecute(String result) {  
        	setContentView(R.layout.activity_search);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) findViewById(R.id.searchResults);
            myWebView.loadData(result, "text/html", null);
        }   
    }
    
	// Uploads XML from last.fm, parses it
	private String loadXmlFromNetwork(String urlString)
			throws XmlPullParserException, IOException {
		InputStream stream = null;
		// Instantiate the parser
		XmlParser xp = new XmlParser();
		
		List<Track> tracks = null;
		/*
		String name = null;
		String artist = null;
		String url = null;
		String imageUrl = null;
		*/
				
		StringBuilder htmlString = new StringBuilder();
		
		try {
			stream = downloadUrl(urlString);
			tracks = xp.parse(stream);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		
	    for (Track track : tracks) {       
	        htmlString.append("<p><a href='");
	        htmlString.append(track.getUrl());
	        htmlString.append("'>" + track.getArtist() + " - " + track.getName() + "</a></p>");
	    }
	    
	    Log.i(SearchActivity.class.getName(), htmlString.toString());
	    return htmlString.toString();
		
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		return conn.getInputStream();
	}
}
