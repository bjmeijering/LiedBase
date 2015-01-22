package org.gkvassenpeelo.liedbase.slidemachine.model;

public class BiblePartFragment {

    public enum DisplayType {
        normal, superScript, italic
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
