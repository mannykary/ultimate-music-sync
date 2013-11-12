package com.mannykary.ultimatemusicsync;

public class Track {
    private String name;
    private String artist;
    private String url;
    private String imageUrl;
    
    public String getName() { 
    	return name; 
    }
    
    public void setName(String name) { 
    	this.name = name; 
    }
    
    public String getArtist() {
    	return artist;
    }
    
    public void setArtist(String artist) {
    	this.artist = artist;
    }
    
    public String getUrl() {
    	return url;
    }
    
    public void setUrl(String url) {
    	this.url = url;
    }
    
    public String getImageUrl() {
    	return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
    	this.imageUrl = imageUrl;
    }
    
    @Override
    public String toString() {
    	return artist + " - " + name;
    }
    
    
}
