package org.gkvassenpeelo.liedbase.songbook;

public class SongLine {
	
	public enum DisplayType {
        normal, chorusTitle, chorusLine
    };

    private DisplayType displayType;

    private String content;

    public SongLine(DisplayType displayType, String content) {
        this.displayType = displayType;
        this.content = content;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public String getContent() {
        return content;
    }
}
