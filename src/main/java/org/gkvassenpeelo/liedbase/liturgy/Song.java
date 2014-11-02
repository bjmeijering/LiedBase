package org.gkvassenpeelo.liedbase.liturgy;



public class Song extends SlideContents {
    
    private String verseNumber = "";
    
    public Song(String header, String body) {
        super(header, body);
    }

    public Song() {
        super();
    }
    
    public void setVerseNumber(String verseNumber) {
        this.verseNumber = verseNumber;
    }
    
    public String getVerseNumber() {
        return verseNumber;
    }
    
}
