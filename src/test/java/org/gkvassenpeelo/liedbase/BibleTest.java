package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.slidemachine.model.BiblePartFragment;
import org.junit.Test;

public class BibleTest extends Bible {

    Bible bijbel = new Bible();

    @Test
    public void getBiblePartTest() throws Exception {
        List<BiblePartFragment> bp = Bible.getBiblePart("nbv", "Exodus", 2, 1, 5);

        for (BiblePartFragment bpf : bp) {
            System.out.print("[" + bpf.getDisplayType().toString() + "]" + bpf.getContent());
        }
    }

    @Test
    public void getBiblePartNonExistentTest() throws Exception {

        try {
            Bible.getBiblePart("nbv", "Exoddus", 2, 1, 5);
        } catch (BibleException e) {
            assertEquals("Boek Exoddus in vertaling NBV niet gevonden", e.getMessage());
        }

    }

    @Test
    public void getBibleBookTest() {
        try {
            assertEquals("Genisis", Bible.getBibleBookFromLine("Gen 1: 4 - 8"));
        } catch (BibleException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void downloadBibleOT() throws Exception {
        
        Map<String, String> bibleBooksOT = new HashMap<String, String>();
        bibleBooksOT.put("Genesis", "50");
        bibleBooksOT.put("Exodus", "40");
        bibleBooksOT.put("Leviticus", "27");
        bibleBooksOT.put("Numeri", "36");
        bibleBooksOT.put("Deuteronomium", "34");
        bibleBooksOT.put("Jozua", "24");
        bibleBooksOT.put("Rechters", "21");
        bibleBooksOT.put("Ruth", "4");
        bibleBooksOT.put("1+Samuel", "31");
        bibleBooksOT.put("2+Samuel", "24");
        bibleBooksOT.put("1+Koningen", "22");
        bibleBooksOT.put("2+Koningen", "25");
        bibleBooksOT.put("1+Kronieken", "29");
        bibleBooksOT.put("2+Kronieken", "36");
        bibleBooksOT.put("Ezra", "10");
        bibleBooksOT.put("Nehemia", "13");
        bibleBooksOT.put("Ester", "10");
        bibleBooksOT.put("Job", "42");
        bibleBooksOT.put("Psalmen", "150");
        bibleBooksOT.put("Spreuken", "31");
        bibleBooksOT.put("Prediker", "12");
        bibleBooksOT.put("Hooglied", "8");
        bibleBooksOT.put("Jesaja", "66");
        bibleBooksOT.put("Jeremia", "52");
        bibleBooksOT.put("Klaagliederen", "5");
        bibleBooksOT.put("Ezechiel", "48");
        bibleBooksOT.put("Daniel", "12");
        bibleBooksOT.put("Hosea", "14");
        bibleBooksOT.put("Joel", "4");
        bibleBooksOT.put("Amos", "9");
        bibleBooksOT.put("Obadja", "1");
        bibleBooksOT.put("Jona", "4");
        bibleBooksOT.put("Micha", "7");
        bibleBooksOT.put("Nahum", "3");
        bibleBooksOT.put("Habakuk", "3");
        bibleBooksOT.put("Sefanja", "3");
        bibleBooksOT.put("Haggai", "2");
        bibleBooksOT.put("Zacharia", "14");
        bibleBooksOT.put("Maleachi", "4");
        
        for (Entry<String, String> e : bibleBooksOT.entrySet()) {
            bijbel.downloadAndSaveBibleBook(e.getKey(), e.getValue(), "BGT");
        }
        
    }

    @Test
    public void downloadBibleNT() throws Exception {

        Map<String, String> bibleBooksOT = new HashMap<String, String>();
//        bibleBooksOT.put("matteus", "28");
//        bibleBooksOT.put("marcus", "16");
//        bibleBooksOT.put("lucas", "24");
//        bibleBooksOT.put("johannes", "21");
//        bibleBooksOT.put("handelingen", "28");
//        bibleBooksOT.put("romeinen", "16");
//        bibleBooksOT.put("1+korintiers", "16");
//        bibleBooksOT.put("2+korintiers", "13");
//        bibleBooksOT.put("galaten", "6");
//        bibleBooksOT.put("efeziers", "6");
        bibleBooksOT.put("filippenzen", "4");
//        bibleBooksOT.put("kolossenzen", "4");
//        bibleBooksOT.put("1+tessalonicenzen", "5");
//        bibleBooksOT.put("2+tessalonicenzen", "3");
//        bibleBooksOT.put("1+timoteus", "6");
//        bibleBooksOT.put("2+timoteus", "4");
//        bibleBooksOT.put("titus", "3");
//        bibleBooksOT.put("filemon", "1");
//        bibleBooksOT.put("hebreeen", "13");
//        bibleBooksOT.put("jakobus", "5");
//        bibleBooksOT.put("1+petrus", "5");
//        bibleBooksOT.put("2+petrus", "3");
//        bibleBooksOT.put("1+johannes", "5");
//        bibleBooksOT.put("2+johannes", "1");
//        bibleBooksOT.put("3+johannes", "2");
//        bibleBooksOT.put("judas", "1");
//        bibleBooksOT.put("openbaring", "22");

        for (Entry<String, String> e : bibleBooksOT.entrySet()) {
            bijbel.downloadAndSaveBibleBook(e.getKey(), e.getValue(), "BGT");
        }

    }

}
