package org.gkvassenpeelo.liedbase.liturgy;

import java.util.HashMap;
import java.util.Map;


public class Song extends SlideContents {
    
    public Song(String header, String body) {
        super(header, body);
        // TODO Auto-generated constructor stub
    }

    public Song() {
        super();
    }

    private Map<String,String> verses = new HashMap<String,String>();
    
    public void addVerse(String verseNumber, String text) {
        verses.put(verseNumber, text);
    }
}
