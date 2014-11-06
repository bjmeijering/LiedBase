package org.gkvassenpeelo.liedbase.liturgy;

public class EndOfMorningService extends SlideContents {

    private String time;

    private String vicarName;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setVicarName(String name) {
        this.vicarName = name;
    }

    public String getVicarName() {
        return vicarName;
    }

}
