package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LiedBaseTest {

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
        lb.setTargetFile(new File("target/Presentatie.pptx"));
    }

    @Test
    public void liedBaseTest() {

        lb.setSourceFile(new File("src/test/resources/liturgie.txt"));

        try {
            lb.parseLiturgyScript();
        } catch (BibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        lb.createSlides();
        lb.save();

    }

    @Ignore
    public void getTimeFromLineTest() {
        assertEquals("", lb.getTimeFromLine(""));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00,"));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00, dominee"));
        assertEquals("dominee l.l", lb.getNextVicarFromLine("einde morgendienst: 15:00, dominee l.l"));
    }

}
