package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class LiturgyModel {

    private Logger logger = Logger.getLogger(LiturgyModel.class);

    private List<LiturgyItem> liturgyItems = new LinkedList<LiturgyItem>();
    private List<LiturgyItem.Type> litugyOverViewItems = new ArrayList<LiturgyItem.Type>();

    public LiturgyModel() {
        // all following liturgy part types will appear on liturgy overview
        // slides
        litugyOverViewItems.add(LiturgyItem.Type.scripture);
        litugyOverViewItems.add(LiturgyItem.Type.song);
    }

    public void addLiturgyItem(LiturgyItem liturgyItem) {
        liturgyItems.add(liturgyItem);
    }

}
