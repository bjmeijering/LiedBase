package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachine;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachineException;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;
import org.junit.Before;
import org.junit.Test;

public class LiedBaseTest {

	// TODO: agenda

	LiedBase lb = new LiedBase();

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void liedBasetest() throws LiedBaseError, Docx4JException, BibleException, SlideMachineException, PaperMachineException {

		lb.setSourceFile(new File("src/test/resources/liturgie.txt"));

		lb.parseLiturgyScript();

//		SlideMachine slideMachine = new SlideMachine(lb.getLiturgy(), lb.getLiturgyView());
//		slideMachine.createSlides();
//		slideMachine.setTargetFile(new File("D:/Projects/LiedBase/target/Presentatie.pptx"));
//		slideMachine.save();

		PaperMachine pm = new PaperMachine(lb.getLiturgy());
		pm.createDocument();
		pm.save("D:/Projects/LiedBase/target/LiturgieBoekje.docx");
	}

	@Test
	public void getTimeFromLineTest() {
		assertEquals("", lb.getTimeFromLine(""));
		assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00,"));
		assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00, dominee"));
	}

	@Test
	public void getNextVicarFromLineTest() {
		assertEquals("dominee l.l", lb.getNextVicarFromLine("einde morgendienst: 15:00, dominee l.l"));
	}

}
