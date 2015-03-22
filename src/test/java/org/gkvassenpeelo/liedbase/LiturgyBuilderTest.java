package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachine;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachineException;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LiturgyBuilderTest {

	LiturgyModel lb = new LiturgyModel(null);

	@Before
	public void setUp() throws Exception {

	}

	@Test
	@Ignore
	public void liedBasetest() throws LiedBaseError, Docx4JException, BibleException, SlideMachineException, PaperMachineException {

		lb.parseLiturgyScript("");

		SlideMachine slideMachine = new SlideMachine(lb);
		slideMachine.createSlides();
		slideMachine.setTargetFile(new File("target/Presentatie.pptx"));
		slideMachine.save();

		PaperMachine pm = new PaperMachine(lb.getLiturgy());
		pm.createDocument();
		pm.setTargetFile(new File("target/LiturgieBoekje.docx"));
		pm.save();
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
