package org.gkvassenpeelo.liedbase.slidemachine.model;

public class EndMorningService extends GenericSlideContent {
    
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
