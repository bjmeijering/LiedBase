package org.gkvassenpeelo.liedbase.liturgy;

public class Welcome extends SlideContents {

    private String vicarName = "";
    
    public Welcome(String vicarName) {
        this.vicarName = vicarName;
    }
    
    public String getVicarName() {
        return vicarName;
    }

}
