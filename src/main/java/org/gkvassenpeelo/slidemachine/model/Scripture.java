package org.gkvassenpeelo.slidemachine.model;

import java.util.List;

public class Scripture extends GenericSlideContent {

    private List<BiblePartFragment> biblePart;

    public Scripture(List<BiblePartFragment> biblePart) {
        this.biblePart = biblePart;
    }

    public List<BiblePartFragment> getBiblePart() {
        return this.biblePart;
    }
}
