package org.gkvassenpeelo.liedbase.bible;

public class BiblePartFragment {

    public enum DisplayType {
        normal, superScript, italic, line_end
    };

    private DisplayType displayType;

    private String content;

    public BiblePartFragment(DisplayType displayType, String content) {
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
