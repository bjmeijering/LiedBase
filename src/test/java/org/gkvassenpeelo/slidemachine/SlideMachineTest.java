package org.gkvassenpeelo.slidemachine;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import org.gkvassenpeelo.liedbase.liturgy.LiturgyParseResult;
import org.gkvassenpeelo.liedbase.liturgy.Parser;
import org.gkvassenpeelo.liedbase.liturgy.SongSlide;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SlideMachineTest {

	@Before
	public void setUp() throws Exception {
	}

	@Ignore
	public void testSlideMachine() throws SlideMachineException {
		SlideMachine sm = new SlideMachine(null);
		sm.setTargetFilename("/target/presentation.pptx");

		SongSlide song = new SongSlide();
		song.setHeader("Samen in de naam van Jezus");
		song.setBody(
				"Samen in de naam van Jezus" + System.getProperty("line.separator") + "Hef ik hier mijn loflied aan");
	}

	@Test
	public void testCreateMdFile() throws SlideMachineException, ParseException, IOException {
		Parser parser = new Parser();
		parser.setText("welkom: naam domi\nVotum\nliedboek 1:1,2\ngezang 12:3\ndaniel 3: 5-7");
		LiturgyParseResult result = parser.parseLiturgyScript();
		assert(!result.hasErrors());
		assert(!result.hasWarnings());
		assertEquals(5, result.getLiturgyItems().size());
		SlideMachine sm = new SlideMachine(result.getLiturgyItems());
		sm.createSlides();
	}

}
