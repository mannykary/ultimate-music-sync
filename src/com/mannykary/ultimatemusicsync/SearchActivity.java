package com.mannykary.ultimatemusicsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ssl.SSLSocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.mannykary.ultimatemusicsync.Track;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

	EditText searchArtistBox, searchTrackBox, userNameField, passField;
	String[] listViewText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    searchArtistBox = (EditText) findViewById(R.id.searchBoxArtist);
	    searchTrackBox = (EditText) findViewById(R.id.searchBoxTrack);
	    userNameField = (EditText) findViewById(R.id.inputUserName);
	    passField = (EditText) findViewById(R.id.inputPass);
	    
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
	
	public void getSearchBoxText(View view) throws UnsupportedEncodingException {

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String qArtist = searchArtistBox.getText().toString().trim();//.replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			String qTrack = searchTrackBox.getText().toString().trim();//.replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			
			//String qArtist = Uri.encode(searchArtistBox.getText().toString().trim(), "utf-8");//.replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			//String qTrack = Uri.encode(searchTrackBox.getText().toString().trim(), "utf-8");//.replace(' ', '+'); // TODO use URL or URI instead to catch malformed urls.
			String userName = userNameField.getText().toString();
			String pass = passField.getText().toString();
			
			Log.i(SearchActivity.class.getName(), "artist: " + qArtist + ", track: " + qTrack);
		/*	
			String qURL = "http://ws.audioscrobbler.com/2.0/?method=track.search&artist=" 
							+ qArtist + "&track=" + qTrack + "&api_key=" 
							+ getString(R.string.lastfm_key);
			
			Log.i(SearchActivity.class.getName(), qURL);
			
			new DownloadXmlTask().execute(qURL);
		*/	
			new LoveTrackTask().execute(qTrack, qArtist, userName, pass);
			
			
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
	
    private class LoveTrackTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
            	String track = params[0];
            	String artist = params[1];
            	String userName = params[2];
            	String pass = params[3];
            	loveTrack(track, artist, userName, pass);
            	
            } catch (IOException e) {
            	e.printStackTrace();
                
            } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null;
        }
    }
	
	public void loveTrack(String track, String artist, String username, String pass) throws NoSuchAlgorithmException, ClientProtocolException, IOException, JSONException {
		HttpClient hc = createHttpClient();
			
		String sk = getSessionKey(username, pass);
		
		String api_sig = "api_key" + getString(R.string.lastfm_key)
				   + "artist" + artist
				   + "methodtrack.love"
				   + "sk" + sk
				   + "track" + track
				   + getString(R.string.lastfm_secret);
		
		Log.i(SearchActivity.class.getName(), "track.love api sig: " + api_sig);
			
/*		byte[] api_sig_bytes = api_sig.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		String api_sig_md5 = md.digest(api_sig_bytes).toString();*/
		
		String api_sig_md5 = md5(api_sig);
		
//		HttpPost hp = new HttpPost("https://ws.audioscrobbler.com/2.0/"
//						+ "?method=track.love"
//						+ "&track=" + track
//					    + "&artist=" + artist 
//					    + "&api_key=" + getString(R.string.lastfm_key)
//					    + "&api_sig" + api_sig_md5
//					    + "&sk" + sk);
		
		HttpPost hp = new HttpPost("https://ws.audioscrobbler.com/2.0/?method=track.love");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("track", track));
	    nameValuePairs.add(new BasicNameValuePair("artist", artist));
	    nameValuePairs.add(new BasicNameValuePair("api_key", getString(R.string.lastfm_key)));
	    nameValuePairs.add(new BasicNameValuePair("api_sig", api_sig_md5));
	    nameValuePairs.add(new BasicNameValuePair("sk", sk));
	    nameValuePairs.add(new BasicNameValuePair("format", "json"));
	    hp.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		HttpResponse resp = hc.execute(hp);
		
		InputStream is = resp.getEntity().getContent(); // convert HTTP response to inputstream
		
		String respStr = inputStreamToString(is).toString();
		
		Log.i(SearchActivity.class.getName(), "resp_love: " + respStr);
		
		JSONObject objMain = new JSONObject(respStr);
		String errorCode = objMain.getString("error");
		String errorMsg = objMain.getString("message");
		
		if (Integer.parseInt(errorCode) == 6) {
			String qURL = "http://ws.audioscrobbler.com/2.0/?method=track.search&artist=" 
					+ artist + "&track=" + track + "&api_key=" 
					+ getString(R.string.lastfm_key);
			Log.i(SearchActivity.class.getName(), "search url: " + qURL);
			new DownloadXmlTask().execute(qURL);
		}
		
		
	}
    
	public String getSessionKey(String username, String pass) throws NoSuchAlgorithmException, ClientProtocolException, IOException, JSONException {
		HttpClient hc = createHttpClient();
		
		String api_sig = "api_key" + getString(R.string.lastfm_key)
					   + "methodauth.getMobileSession"
					   + "password" + pass
					   + "username" + username
					   + getString(R.string.lastfm_secret);
		
		Log.i(SearchActivity.class.getName(), "auth.getMobileSession api sig: " + api_sig);
				
/*		byte[] api_sig_bytes = api_sig.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		String api_sig_md5 = md.digest(api_sig_bytes).toString();*/
		
		String api_sig_md5 = md5(api_sig);
		
		Log.i(SearchActivity.class.getName(), "auth.getMobileSession api_sig md5: " + api_sig_md5);
				
		HttpPost hp_auth = new HttpPost("https://ws.audioscrobbler.com/2.0/?method=auth.getMobileSession");
		
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("username", username));
	    nameValuePairs.add(new BasicNameValuePair("password", pass));
	    nameValuePairs.add(new BasicNameValuePair("api_key", getString(R.string.lastfm_key)));
	    nameValuePairs.add(new BasicNameValuePair("api_sig", api_sig_md5));
	    nameValuePairs.add(new BasicNameValuePair("format", "json"));
	    hp_auth.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
		
//		String params = "?method=auth.getMobileSession" 
//					+ "&username=" + username 
//					+ "&password=" + pass 
//					+ "&api_key=" + getString(R.string.lastfm_key)
//					+ "&api_sig=" + api_sig_md5);
		
		
		
		HttpResponse resp = hc.execute(hp_auth);
		InputStream is = resp.getEntity().getContent(); // convert HTTP response to inputstream
		
		String respStr = inputStreamToString(is).toString();
		
		Log.i(SearchActivity.class.getName(), "resp: " + respStr);
		
		JSONObject objMain = new JSONObject(respStr);
		JSONObject objSession = objMain.getJSONObject("session");
		String name = objSession.getString("name");
		String key = objSession.getString("key");
		String subscriber = objSession.getString("subscriber");
		return key;
			
				
/*		
		// TODO FIX XML PARSER
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		Session session = null;
		String text = null;
		
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			
			parser.setInput(is, null);
			
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = parser.getName();
				
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if (tagName.equalsIgnoreCase("session")) {
						session = new Session();
					}
					break;
					
				case XmlPullParser.TEXT:
					text = parser.getText();
					Log.i(SearchActivity.class.getName(), "xml text: " + text);
					break;
					
				case XmlPullParser.END_TAG:
					if (tagName.equalsIgnoreCase("name")) {
						session.setName(text);
					} else if (tagName.equalsIgnoreCase("key")) {
						session.setKey(text);
						Log.i(SearchActivity.class.getName(), "xml text for key: " + text);
					} else if (tagName.equalsIgnoreCase("subscriber")) {
						session.setSubscriber(text);
					} 
					break;
				
				default:
					break;
				}
				eventType = parser.next();
			}
			
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			
			parser.setInput(is, null);
			
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = parser.getName();
				
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if (tagName.equalsIgnoreCase("name")) {
						//TODO not sure what to put here...
					}
					break;
					
				case XmlPullParser.TEXT:
					text = parser.getText();
					break;
					
				case XmlPullParser.END_TAG:
					if (tagName.equalsIgnoreCase("name")) {
						name = text;
					} else if (tagName.equalsIgnoreCase("key")) {
						key = text;
					}
					break;
				
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return session.getKey();*/
	}
	
	private StringBuilder inputStreamToString(InputStream is) throws IOException {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    while ((line = rd.readLine()) != null) { 
	        total.append(line); 
	    }
	    
	    // Return full string
	    return total;
	}
	
	private String md5(String in) {
	    MessageDigest digest;
	    try {
	        digest = MessageDigest.getInstance("MD5");
	        digest.reset();
	        digest.update(in.getBytes());
	        byte[] a = digest.digest();
	        int len = a.length;
	        StringBuilder sb = new StringBuilder(len << 1);
	        for (int i = 0; i < len; i++) {
	            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
	            sb.append(Character.forDigit(a[i] & 0x0f, 16));
	        }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
	    return null;
	}
	
	private HttpClient createHttpClient()
	{
	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);

	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    return new DefaultHttpClient(conMgr, params);
	}

}
