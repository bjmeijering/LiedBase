package org.gkvassenpeelo.liedbase;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Before;
import org.junit.Test;

public class LiedBaseTest {

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
        lb.setTargetFile("/target/Presentation.pptx");
    }

    @Test
    public void liedBasetest() throws LiedBaseError, Docx4JException {

        lb.parseLiturgyScript("dominee L.E. Leeftink" + System.getProperty("line.separator") 
                + "votum en zegengroet" + System.getProperty("line.separator") 
                + "gezang 1:1,2,3" + System.getProperty("line.separator") 
                + "gebed" + System.getProperty("line.separator")
                + "Psalm 100:2, 4" + System.getProperty("line.separator")
                + "collecte" + System.getProperty("line.separator")
                + "Gebed" + System.getProperty("line.separator")
                + "liedboek 119:1, 6" + System.getProperty("line.separator")
                + "amen" + System.getProperty("line.separator")
                + "einde middagdienst"
                );
        lb.createSlides();
        lb.save();
    }

}
