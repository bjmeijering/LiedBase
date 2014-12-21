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
        List<BiblePartFragment> bp = Bible.getBiblePart("nbv", "Hebreeen", 3, 7, 11);

        for (BiblePartFragment bpf : bp) {
            System.out.print("[" + bpf.getDisplayType().toString() + "]" + bpf.getContent());
//            System.out.print(bpf.getContent());
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

    @Test
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
