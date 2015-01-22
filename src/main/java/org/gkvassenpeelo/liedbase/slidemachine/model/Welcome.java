package org.gkvassenpeelo.liedbase.slidemachine.model;

public class Welcome extends GenericSlideContent {

    private String vicarName = "";
    
    public Welcome(String vicarName) {
        this.vicarName = vicarName;
    }
    
    public Object getVicarName() {
        return vicarName;
    }

}
