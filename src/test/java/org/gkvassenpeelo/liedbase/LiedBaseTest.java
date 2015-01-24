package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LiedBaseTest {
    
    // TODO: agenda

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
    	
    }

    @Ignore
    public void liedBasetest() throws LiedBaseError, Docx4JException, BibleException {

        lb.setSourceFile(new File("src/test/resources/liturgie.txt"));

        lb.parseLiturgyScript();

    }

    @Test
    public void getTimeFromLineTest() {
        assertEquals("", lb.getTimeFromLine(""));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00,"));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00, dominee"));
        assertEquals("dominee l.l", lb.getNextVicarFromLine("einde morgendienst: 15:00, dominee l.l"));
    }

}
