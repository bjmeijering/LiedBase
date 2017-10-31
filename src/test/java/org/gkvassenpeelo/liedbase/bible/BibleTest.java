package org.gkvassenpeelo.liedbase.bible;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

public class BibleTest {

    Bible bijbel = new Bible();

    @Test
    public void getBibleBookFromLineTest() throws BibleException {

        try {
            Bible.getBibleBookFromLine("");
        } catch (BibleException e) {
            assertEquals("Bijbelboek niet gevonden in liturgieregel: ", e.getMessage());
        }

        try {
            Bible.getBibleBookFromLine("fiets");
        } catch (BibleException e) {
            assertEquals("Bijbelboek niet gevonden in liturgieregel: fiets", e.getMessage());
        }

        try {
            assertEquals("Genesis", Bible.getBibleBookFromLine("genesis 3: 2-3"));
            assertEquals("Exodus", Bible.getBibleBookFromLine("eXoddues 3: 2-3"));
            assertEquals("Dani�l", Bible.getBibleBookFromLine(" daniel 1"));
            assertEquals("2 Johannes", Bible.getBibleBookFromLine("2 johannus 14: 34 - 10000"));
            assertEquals("Openbaring", Bible.getBibleBookFromLine("openbaringngn 2: 2-3"));
            assertEquals("Matte�s", Bible.getBibleBookFromLine("mattheus 2: 2-2"));
        } catch (BibleException e) {
            fail("Something went wrong: " + e.getMessage());
        }
    }

    @Test
    public void getBiblePartTestFromText() throws Exception {
        // test getfirst verse of a first chapter
        List<BiblePartFragment> bp = Bible.getBiblePartFromText("bgt", "genesis", 1, 1, 2);

        assertEquals("superScript", bp.get(0).getDisplayType().toString());
        assertEquals("1", bp.get(0).getContent());
        assertEquals("normal", bp.get(1).getDisplayType().toString());
        assertEquals("In het begin maakte God de hemel en de aarde.", bp.get(1).getContent());

        // test a verse from the middle of a chapter
        bp = Bible.getBiblePartFromText("bgt", "genesis", 1, 13, 17);

        assertEquals("superScript", bp.get(0).getDisplayType().toString());
        assertEquals("13", bp.get(0).getContent());
        assertEquals("normal", bp.get(1).getDisplayType().toString());
        assertEquals("Toen werd het avond en het werd ochtend. Dat was de derde dag.", bp.get(1).getContent());
    }

    @Test
    public void getChapterFromLineTest() throws Exception {
        assertEquals(2, Bible.getChapterFromLine("genesis 2: 13"));
        assertEquals(2, Bible.getChapterFromLine(" genesis 2 : 13"));
        assertEquals(2, Bible.getChapterFromLine("2 johannes 2 : 13"));
        assertEquals(2, Bible.getChapterFromLine("nietb estaadn 2 : 13"));
    }

    @Test
    public void getStartVerseFromLineTest() {
        assertEquals(13, Bible.getStartVerseFromLine("genesis 2: 13 (BGT)"));
        assertEquals(13, Bible.getStartVerseFromLine("genesis 2: 13"));
        assertEquals(2, Bible.getStartVerseFromLine("genesis 2: 2-13"));
        assertEquals(2, Bible.getStartVerseFromLine("genesis 2: 2 - 13"));
        assertEquals(2, Bible.getStartVerseFromLine("genesis 2: 2 - 13 "));
    }

    @Test
    public void getEndVerseFromLineTest() {
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 13 (BGT)"));
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 13"));
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 2-13"));
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 2 - 13"));
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 2 - 13 "));
    }

    @Test
    public void getBiblePartNonExistentTest() throws Exception {

        try {
            Bible.getBiblePartFromText("nbv", "Exoddus", 2, 1, 5);
        } catch (BibleException e) {
            assertEquals("Boek exoddus in vertaling NBV niet gevonden", e.getMessage());
        }

    }

    @Test
    public void getBibleBookTest() {
        try {
            assertEquals("Genesis", Bible.getBibleBookFromLine("Gen 1: 4 - 8"));
        } catch (BibleException e) {
            fail(e.getMessage());
        }
    }

}
