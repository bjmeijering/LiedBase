package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class Gathering extends SlideContents {

    private List<String> benificiaries = new ArrayList<String>();

    public Gathering(List<String> gatheringBenificiaries) {
        benificiaries = gatheringBenificiaries;
    }
    
    public String getFirstGatheringBenificiary() {
        return benificiaries.get(0);
    }
    
    public String getSecondGatheringBenificiary() {
        return benificiaries.get(1);
    }

}
