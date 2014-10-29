package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class LiturgyPart {
    
    private List<SlideContents> slides = new ArrayList<SlideContents>();
    
    public void addSlide(SlideContents slide) {
        slides.add(slide);
    }
    
    public List<SlideContents> getSlides() {
        return slides;
    }
    
}
