package com.mannykary.ultimatemusicsync;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import android.util.Xml;

public class XmlParser {

	List<Track> tracks;
	private Track track;
	private String text;
	
	public XmlParser() {
		tracks = new ArrayList<Track>();
	}
	
	public List<Track> getTracks() {
		return tracks;
	}
	
	public List<Track> parse(InputStream is) {
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		
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
					if (tagName.equalsIgnoreCase("track")) {
						track = new Track();
					}
					break;
					
				case XmlPullParser.TEXT:
					text = parser.getText();
					break;
					
				case XmlPullParser.END_TAG:
					if (tagName.equalsIgnoreCase("track")) {
						tracks.add(track);
					} else if (tagName.equalsIgnoreCase("name")) {
						track.setName(text);
					} else if (tagName.equalsIgnoreCase("artist")) {
						track.setArtist(text);
					} else if (tagName.equalsIgnoreCase("url")) {
						track.setUrl(text);
					} /*else if (tagName.equalsIgnoreCase("image")) {
						if (parser.getAttributeValue(null, "size").equals("small")) {
							track.setImageUrl(text);
						}
					}*/
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
		
		return tracks;
	}
	
	/*
	// We don't use namespaces
	private static final String ns = null;

	public List<Track> parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);		
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}

	private List readFeed(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		List entries = new ArrayList();

		traverse("lfm", parser);
		
		Log.i(XmlParser.class.getName(), parser.getName() + " =? results");
		if (parser.getName().equals("results")) {
			traverse("results", parser);
			
			Log.i(XmlParser.class.getName(), parser.getName() + " =? trackmatches");
			if (parser.getName().equals("trackmatches")) {
				traverse("trackmatches", parser);
				
				Log.i(XmlParser.class.getName(), parser.getName() + " =? track");
				if (parser.getName().equals("track")) {
					entries.add(readTrack(parser));
				} else {
					skip(parser);
				}
			} else {
				skip(parser);
			}
		} else {
			skip(parser);
		}
		
		return entries;
		
		
		//parser.require(XmlPullParser.START_TAG, ns, "lfm");
	
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			//String name = parser.getName();
			if (parser.getName().equals("results")){
				parser.require(XmlPullParser.START_TAG, ns, "entry");
			}
			
			
			// Starts by looking for the entry tag
			if (name.equals("track")) {
				entries.add(readTrack(parser));
			} else {
				skip(parser);
			}
		}
		
	}
	
	public void traverse(String tagName, XmlPullParser parser) throws XmlPullParserException,
				IOException {
		parser.require(XmlPullParser.START_TAG, ns, tagName);
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
		}
	}

	public static class Track {
	    public final String name;
	    public final String artist;
	    public final String url;
	    public final String imageUrl;

	    Track(String name, String artist, String url, String imageUrl) {
	        this.name = name;
	        this.artist = artist;
	        this.url = url;
	        this.imageUrl = imageUrl;
	    }
	}
	
	// Parses the contents of an entry. If it encounters a title, summary, or
	// link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the
	// tag.
	private Track readTrack(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "track");
		String name = null;
		String artist = null;
		String url = null;
		String imageUrl = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String tagName = parser.getName();
			if (tagName.equals("name")) {
				name = readName(parser);
			} else if (tagName.equals("artist")) {
				artist = readArtist(parser);
			} else if (tagName.equals("url")) {
				url = readUrl(parser);
			} else if (tagName.equals("image")) {
				imageUrl = readImageUrl(parser);
			} else {
				skip(parser);
			}
		}
		return new Track(name, artist, url, imageUrl);
	}

	// Processes track tags in the feed.
	private String readName(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String name = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");
		return name;
	}

	// Processes artist tags in the feed.
	private String readArtist(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "artist");
		String artist = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "artist");
		return artist;
	}

	// Processes url tags in the feed.
	private String readUrl(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "url");
		String url = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "url");
		return url;
	}
	
	// Processes image tags in the feed.
	private String readImageUrl(XmlPullParser parser) throws IOException, 
			XmlPullParserException {
	    String imageUrl = "";
	    parser.require(XmlPullParser.START_TAG, ns, "image");
	    String tag = parser.getName();
	    String imageSize = parser.getAttributeValue(null, "size");  
	    if (tag.equals("size")) {
	        if (imageSize.equals("small")){
	            imageUrl = readText(parser);
	            parser.nextTag();
	        } 
	    }
	    parser.require(XmlPullParser.END_TAG, ns, "image");
	    return imageUrl;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	 
*/
	
}
