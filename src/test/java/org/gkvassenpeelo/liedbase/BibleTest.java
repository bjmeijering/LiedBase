package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;
import org.junit.Ignore;
import org.junit.Test;

public class BibleTest extends Bible {

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
            assertEquals("Daniël", Bible.getBibleBookFromLine(" daniel 1"));
            assertEquals("2 Johannes", Bible.getBibleBookFromLine("2 johannus 14: 34 - 10000"));
            assertEquals("Openbaring", Bible.getBibleBookFromLine("openbaringngn 2: 2-3"));
            assertEquals("Matteüs", Bible.getBibleBookFromLine("mattheus 2: 2-2"));
        } catch (BibleException e) {
            fail("Something went wrong: " + e.getMessage());
        }
    }

    @Test
    public void getBiblePartTest() throws Exception {
        List<BiblePartFragment> bp = Bible.getBiblePart("bgt", "genesis", 1, 1, 2);

        assertEquals("superScript", bp.get(0).getDisplayType().toString());
        assertEquals("1", bp.get(0).getContent());
        assertEquals("normal", bp.get(1).getDisplayType().toString());
        assertEquals("In het begin maakte God de hemel en de aarde. ", bp.get(1).getContent());
    }
    
    @Test
    public void getChapterFromLineTest() {
        assertEquals(2, Bible.getChapterFromLine("genesis 2: 13"));
        assertEquals(2, Bible.getChapterFromLine(" genesis 2 : 13"));
        assertEquals(2, Bible.getChapterFromLine("2 johannes 2 : 13"));
        assertEquals(2, Bible.getChapterFromLine("nietb estaadn 2 : 13"));
    }
    
    @Test
    public void getEndVerseFromLineTest() {
        assertEquals(13, Bible.getEndVerseFromLine("genesis 2: 13"));
    }

    @Test
    public void getBiblePartNonExistentTest() throws Exception {

        try {
            Bible.getBiblePart("nbv", "Exoddus", 2, 1, 5);
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

    @Ignore
    public void downloadBibleOT() throws Exception {

        Map<String, String> bibleBooksOT = new HashMap<String, String>();
        bibleBooksOT.put("genesis", "50");
        bibleBooksOT.put("exodus", "40");
        bibleBooksOT.put("leviticus", "27");
        bibleBooksOT.put("numeri", "36");
        bibleBooksOT.put("deuteronomium", "34");
        bibleBooksOT.put("jozua", "24");
        bibleBooksOT.put("rechters", "21");
        bibleBooksOT.put("ruth", "4");
        bibleBooksOT.put("1+Samuel", "31");
        bibleBooksOT.put("2+Samuel", "24");
        bibleBooksOT.put("1+Koningen", "22");
        bibleBooksOT.put("2+Koningen", "25");
        bibleBooksOT.put("1+Kronieken", "29");
        bibleBooksOT.put("2+Kronieken", "36");
        bibleBooksOT.put("ezra", "10");
        bibleBooksOT.put("nehemia", "13");
        bibleBooksOT.put("ester", "10");
        bibleBooksOT.put("job", "42");
        bibleBooksOT.put("psalmen", "150");
        bibleBooksOT.put("spreuken", "31");
        bibleBooksOT.put("prediker", "12");
        bibleBooksOT.put("hooglied", "8");
        bibleBooksOT.put("jesaja", "66");
        bibleBooksOT.put("jeremia", "52");
        bibleBooksOT.put("klaagliederen", "5");
        bibleBooksOT.put("ezechiel", "48");
        bibleBooksOT.put("daniel", "12");
        bibleBooksOT.put("hosea", "14");
        bibleBooksOT.put("joel", "4");
        bibleBooksOT.put("amos", "9");
        bibleBooksOT.put("obadja", "1");
        bibleBooksOT.put("jona", "4");
        bibleBooksOT.put("micha", "7");
        bibleBooksOT.put("nahum", "3");
        bibleBooksOT.put("habakuk", "3");
        bibleBooksOT.put("sefanja", "3");
        bibleBooksOT.put("haggai", "2");
        bibleBooksOT.put("zacharia", "14");
        bibleBooksOT.put("maleachi", "4");

        for (Entry<String, String> e : bibleBooksOT.entrySet()) {
            bijbel.downloadAndSaveBibleBook(e.getKey(), e.getValue(), "SV77");
        }

    }

    @Ignore
    public void downloadBibleNT() throws Exception {

        Map<String, String> bibleBooksOT = new HashMap<String, String>();
        bibleBooksOT.put("matteus", "28");
        bibleBooksOT.put("marcus", "16");
        bibleBooksOT.put("lucas", "24");
        bibleBooksOT.put("johannes", "21");
        bibleBooksOT.put("handelingen", "28");
        bibleBooksOT.put("romeinen", "16");
        bibleBooksOT.put("1+korintiers", "16");
        bibleBooksOT.put("2+korintiers", "13");
        bibleBooksOT.put("galaten", "6");
        bibleBooksOT.put("efeziers", "6");
        bibleBooksOT.put("filippenzen", "4");
        bibleBooksOT.put("kolossenzen", "4");
        bibleBooksOT.put("1+tessalonicenzen", "5");
        bibleBooksOT.put("2+tessalonicenzen", "3");
        bibleBooksOT.put("1+timoteus", "6");
        bibleBooksOT.put("2+timoteus", "4");
        bibleBooksOT.put("titus", "3");
        bibleBooksOT.put("filemon", "1");
        bibleBooksOT.put("hebreeen", "13");
        bibleBooksOT.put("jakobus", "5");
        bibleBooksOT.put("1+petrus", "5");
        bibleBooksOT.put("2+petrus", "3");
        bibleBooksOT.put("1+johannes", "5");
        bibleBooksOT.put("2+johannes", "1");
        bibleBooksOT.put("3+johannes", "2");
        bibleBooksOT.put("judas", "1");
        bibleBooksOT.put("openbaring", "22");

        for (Entry<String, String> e : bibleBooksOT.entrySet()) {
            bijbel.downloadAndSaveBibleBook(e.getKey(), e.getValue(), "SV77");
        }

    }

}
