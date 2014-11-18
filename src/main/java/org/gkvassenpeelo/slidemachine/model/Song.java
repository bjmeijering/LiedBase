package org.gkvassenpeelo.slidemachine.model;

public class Song extends GenericSlideContent {
    
    private String verseNumber;

    public String getCurrentVerse() {
        return verseNumber;
    }

    public void setCurrentVerse(String currentVerse) {
        this.verseNumber = currentVerse;
    }

}
