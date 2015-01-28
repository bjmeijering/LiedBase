package org.gkvassenpeelo.liedbase.liturgy;

import java.util.List;

import org.gkvassenpeelo.liedbase.songbook.SongLine;



public class Song extends SlideContents {
    
    private String verseNumber = "";
    private List<SongLine> songText;
    
    public Song(String header, List<SongLine> songText) {
        setHeader(header);
        setSongText(songText);
    }

    private void setSongText(List<SongLine> songText) {
		this.songText = songText;
	}
    
    public List<SongLine> getSongText() {
    	return songText;
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
