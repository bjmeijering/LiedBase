package org.gkvassenpeelo.liedbase.liturgy;

import java.util.LinkedList;
import java.util.List;

public class Liturgy {

    private List<LiturgyPart> liturgy = new LinkedList<LiturgyPart>();

    public void addLiturgyPart(LiturgyPart liturgyPart) {
        liturgy.add(liturgyPart);
    }

    public List<LiturgyPart> getLiturgyParts() {
        return liturgy;
    }
}
