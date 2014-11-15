package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Before;
import org.junit.Test;

public class LiedBaseTest {

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
        lb.setTargetFile(new File("target/Presentatie.pptx"));
    }

    @Test
    public void liedBasetest() throws LiedBaseError, Docx4JException {

        lb.setSourceFile(new File("src/test/resources/liturgie.txt"));

        lb.parseLiturgyScript();
        lb.createSlides();
        lb.save();

    }

    @Test
    public void getTimeFromLineTest() {
        assertEquals("", lb.getTimeFromLine(""));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00,"));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00, dominee"));
        assertEquals("dominee l.l", lb.getNextVicarFromLine("einde morgendienst: 15:00, dominee l.l"));
    }

}
