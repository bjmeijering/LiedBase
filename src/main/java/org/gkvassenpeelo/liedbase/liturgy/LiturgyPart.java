package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class LiturgyPart {
    
    public enum Type {
        welcome, song, gathering, prair, law, lecture, votum, amen, endOfMorningService, endOfAfternoonService, scripture
    };
    
    private Type type;
    
    private List<SlideContents> slides = new ArrayList<SlideContents>();
    
    public LiturgyPart(Type type) {
        setType(type);
    }
    
    public void addSlide(SlideContents slide) {
        slides.add(slide);
    }
    
    public List<SlideContents> getSlides() {
        return slides;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
}
