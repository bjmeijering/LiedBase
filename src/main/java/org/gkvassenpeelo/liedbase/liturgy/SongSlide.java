package org.gkvassenpeelo.liedbase.liturgy;

import java.util.List;

import org.gkvassenpeelo.liedbase.songbook.SongLine;

public class SongSlide extends SlideContents {

    private String verseNumber = "";
    private List<SongLine> songText;

    public SongSlide(String header, List<SongLine> songText) {
        setHeader(header);
        setSongText(songText);
    }

    private void setSongText(List<SongLine> songText) {
        this.songText = songText;
    }

    public List<SongLine> getSongText() {
        return songText;
    }

    public SongSlide() {
        super();
    }

    public void setVerseNumber(String verseNumber) {
        this.verseNumber = verseNumber;
    }

    public String getVerseNumber() {
        return verseNumber;
    }

    /**
     * Build a nicely formatted header with the current versenu,ber in bold.
     */
    public String getHeader() {
        return header.substring(0, 1).toUpperCase() + header.substring(1, header.indexOf(":"))
                + (header.substring(header.indexOf(":"))).replace(verseNumber, "**" + verseNumber + "**");
    }

}
