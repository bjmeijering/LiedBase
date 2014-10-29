package org.gkvassenpeelo.liedbase;

import org.junit.Before;
import org.junit.Test;

public class LiedBaseTest {

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void liedBasetest() throws LiedBaseError {

        lb.parseLiturgyScript("psalm 1:1" + System.getProperty("line.separator") + "Gezang 1:2, 4");
        lb.createSlides();
    }

}
