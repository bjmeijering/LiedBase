package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class Gathering extends SlideContents {

    private List<String> benificiaries = new ArrayList<String>();

    public Gathering(List<String> gatheringBenificiaries) {
        benificiaries = gatheringBenificiaries;
    }

    public String getFirstGatheringBenificiary() {
        if (benificiaries.size() > 0) {
            return benificiaries.get(0);
        }
        return "eerste collecte";
    }

    public String getSecondGatheringBenificiary() {
        if (benificiaries.size() > 1) {
            return benificiaries.get(1);
        }
        return "tweede collecte";
    }

}
