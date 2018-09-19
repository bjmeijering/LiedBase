package org.gkvassenpeelo.liedbase.liturgy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParserTest {

	private Parser parser = new Parser();

	@Test
	public void getStartVerseFromLineTest() {
		assertEquals(13, parser.getVersesFromLine("genesis 2: 13 (BGT)")[0]);
		assertEquals(1, parser.getVersesFromLine("genesis 2: 13 (BGT)").length);
		assertEquals(13, parser.getVersesFromLine("genesis 2: 13")[0]);
		assertEquals(1, parser.getVersesFromLine("genesis 2: 13").length);
		assertEquals(2, parser.getVersesFromLine("genesis 2: 2,3,4")[0]);
		assertEquals(3, parser.getVersesFromLine("genesis 2: 2,3,4")[1]);
		assertEquals(4, parser.getVersesFromLine("genesis 2: 2,3,4")[2]);
		assertEquals(3, parser.getVersesFromLine("genesis 2: 2,3,4").length);
		assertEquals(2, parser.getVersesFromLine("genesis 2: 2 ,  13 ")[0]);
		assertEquals(13, parser.getVersesFromLine("genesis 2: 2 ,  13 ")[1]);
		assertEquals(2, parser.getVersesFromLine("genesis 2: 2 , 13 ").length);
	}

	@Test
	public void getEndVerseFromLineTest() {
		assertEquals(2, parser.getChapterFromLine("genesis 2: 13 (BGT)"));
		assertEquals(2, parser.getChapterFromLine("genesis 2: 13"));
		assertEquals(2, parser.getChapterFromLine("genesis 2: 2-13"));
	}

	@Test
	public void getVerseRangeFromLineTest() {
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 13 (BGT)").getStartVerse());
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 13 (BGT)").getEndVerse());
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 13").getStartVerse());
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 13").getEndVerse());
		assertEquals(2, parser.getVerseRangeFromLine("genesis 2: 2-13").getStartVerse());
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 2-13").getEndVerse());
		assertEquals(2, parser.getVerseRangeFromLine("genesis 2: 2 - 13").getStartVerse());
		assertEquals(13, parser.getVerseRangeFromLine("genesis 2: 2 - 13 ").getEndVerse());
	}

}
