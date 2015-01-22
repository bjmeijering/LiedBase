package org.gkvassenpeelo.liedbase.slidemachine.model;

import java.util.List;

import org.gkvassenpeelo.liedbase.liturgy.SlideContents;

public class Scripture extends GenericSlideContent {
    
    private org.gkvassenpeelo.liedbase.liturgy.Scripture scripture;

    public Scripture(SlideContents sc) {
        this.scripture = (org.gkvassenpeelo.liedbase.liturgy.Scripture)sc;
    }

    public List<BiblePartFragment> getBiblePart() {
        return scripture.getBiblePart();
    }

    public String getBibleBook() {
        return scripture.getBibleBook();
    }

    public int getChapter() {
        return scripture.getChapter();
    }

    public int getFromVerse() {
        return scripture.getFromVerse();
    }

    public int getToVerse() {
        return scripture.getToVerse();
    }
}
