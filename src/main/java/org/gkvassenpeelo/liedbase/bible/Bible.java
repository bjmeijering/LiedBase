package org.gkvassenpeelo.liedbase.bible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.gkvassenpeelo.slidemachine.model.BiblePartFragment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Bible {

    private static final String ENCODING = "UTF-8";

    private static String DEFAULT_TRANSLATION = "NBV";

    private String url = "https://www.debijbel.nl/bijbel/zoeken/%s/%s+%s";

    private static String LINE_END = System.getProperty("line.separator");

    public Bible() {

    }

    public static List<BiblePartFragment> getBiblePart(String translation, String book, int chapter, int fromVerse, int toVerse) throws BibleException {

        translation = translation.toUpperCase();

        book = book.toLowerCase();

        Document doc;
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("bible/" + translation + "/" + book + ".dat");
            doc = Jsoup.parse(in, ENCODING, "");
            doc.outputSettings().charset(ENCODING);
            in.close();
        } catch (IOException e) {
            throw new BibleException(String.format("Boek %s in vertaling %s niet gevonden", book, translation));
        } catch (NullPointerException e) {
            throw new BibleException(String.format("Boek %s in vertaling %s niet gevonden", book, translation));
        }

        Element bibletext = doc.select("div[id=scroller]").get(chapter - 1);

        List<BiblePartFragment> bp = new ArrayList<BiblePartFragment>();

        Elements parts = bibletext.children();
        
        int currentStartVerse = -1;
        int currentEndVerse = 1000;

        String mainHeader = "";
        String header = "";

        for (Element part : parts) {

            // h2 header
            if (part.attributes().get("class").equals("ms")) {
                mainHeader = part.text();
            }

            // h3 header
            if (part.attributes().get("class").equals("s")) {
                header = part.text();
            }

            if (part.children().size() == 0 && part.tag().getName().equals("p")) {
                if (currentStartVerse >= fromVerse && currentEndVerse <= toVerse) {
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, part.text()));
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, LINE_END));
                }
            }

            for (Element subPart : part.children()) {

                // try to capture verse number
                String verseId = subPart.select("sup").text();
                if (!StringUtils.isEmpty(verseId)) {
                    if (verseId.contains("-")) {
                        currentStartVerse = Integer.parseInt(StringUtils.substringBefore(verseId, "-"));
                        currentEndVerse = Integer.parseInt(StringUtils.substringAfter(verseId, "-"));
                    } else {
                        currentStartVerse = currentEndVerse = Integer.parseInt(verseId);
                    }
                }

                // add verse part contents
                if (currentStartVerse >= fromVerse && currentEndVerse <= toVerse) {
                    if (!StringUtils.isEmpty(mainHeader)) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, mainHeader + LINE_END));
                    }
                    if (!StringUtils.isEmpty(header)) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, header + LINE_END));
                    }
                    if (subPart.select("sup").size() > 0) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.superScript, subPart.select("sup").first().text().trim()));
                        subPart.select("sup").first().html("");
                    }
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, subPart.text().trim()));

                    if (part.tag().getName().equals("p")) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, LINE_END));
                    }
                }

                // stop iterating verses
                if (currentStartVerse > toVerse) {
                    break;
                }

                // clear the headers
                mainHeader = "";
                header = "";
            }

            // stop iterating paragraphs
            if (currentStartVerse > toVerse) {
                break;
            }

        }

        return bp;
    }

    private String extractBibleChapterFromHtml(String result) {

        Document doc = Jsoup.parse(result, ENCODING);

        return doc.select("div.bibletext").toString();
    }

    public void downloadAndSaveBibleBook(String book, String maxChapter, String translation) throws Exception {

        File f = new File(translation + "/" + book.toLowerCase().replace("+", "") + ".dat");
        f.getParentFile().mkdirs();

        FileWriter fw = new FileWriter(f);

        try {
            for (int i = 1; i <= Integer.parseInt(maxChapter); i++) {

                // get the page
                String result = getPageContent(String.format(url, translation, book, i));

                fw.append(extractBibleChapterFromHtml(result));
            }
        } finally {
            fw.close();
        }

    }

    public String getPageContent(String url) throws Exception {

        CookieHandler.setDefault(new CookieManager());

        HttpsURLConnection conn;

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("Host", "www.debijbel.nl");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "nl,en-US;q=0.7,en;q=0.3");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.addRequestProperty(
                "Cookie",
                "_ga=GA1.2.276380152.1413559522; nbg_ecmgt_status=implicitconsent; auth_key=aff46db4d4bd04f71a9f62712f590f8b; PHPSESSID=a5g3ik78moco8nj0u440q86ag4".split(";", 1)[0]);

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return Jsoup.parse(response.toString()).toString();

    }

    public static String getTranslationFromLine(String line) throws BibleException {
        if (line.trim().matches(".*\\([a-zA-Z]{1,3}\\)$")) {
            if (line.toLowerCase().trim().endsWith("(nbv)")) {
                return "NBV";
            } else if (line.toLowerCase().trim().endsWith("(bgt)")) {
                return "BGT";
            } else if (line.toLowerCase().trim().endsWith("(nbg)")) {
                return "NBG51";
            } else if (line.toLowerCase().trim().endsWith("(sv)")) {
                return "SV77";
            } else {
                throw new BibleException("Onbekende vertaling in regel: " + line);
            }
        }
        return DEFAULT_TRANSLATION;
    }

    // for each bible book an if statement
    public static String getBibleBookFromLine(String line) throws BibleException {
        if (line.toLowerCase().startsWith("gen")) {
            return "Genesis";
        }
        if (line.toLowerCase().startsWith("exod")) {
            return "Exodus";
        }
        if (line.toLowerCase().startsWith("levi")) {
            return "Leviticus";
        }
        if (line.toLowerCase().startsWith("nume")) {
            return "Numeri";
        }
        if (line.toLowerCase().startsWith("deut")) {
            return "Deuteronomium";
        }
        if (line.toLowerCase().startsWith("jozu")) {
            return "Jozua";
        }
        if (line.toLowerCase().startsWith("rech")) {
            return "Rechters";
        }
        if (line.toLowerCase().startsWith("ruth")) {
            return "Ruth";
        }
        if (line.toLowerCase().matches("^1 ?sam.*")) {
            return "1 Samuel";
        }
        if (line.toLowerCase().matches("^2 ?sam.*")) {
            return "2 Samuel";
        }
        if (line.toLowerCase().matches("^1 ?kon.*")) {
            return "1 Koningen";
        }
        if (line.toLowerCase().matches("^2 ?kon.*")) {
            return "2 Koningen";
        }
        if (line.toLowerCase().matches("^1 ?kro.*")) {
            return "1 Kronieken";
        }
        if (line.toLowerCase().matches("^2 ?kro.*")) {
            return "2 Kronieken";
        }
        if (line.toLowerCase().startsWith("ezra")) {
            return "Ezra";
        }
        if (line.toLowerCase().startsWith("nehe")) {
            return "Nehemia";
        }
        if (line.toLowerCase().startsWith("este")) {
            return "Ester";
        }
        if (line.toLowerCase().startsWith("job")) {
            return "Job";
        }
        if (line.toLowerCase().startsWith("psal")) {
            return "Psalmen";
        }
        if (line.toLowerCase().startsWith("spre")) {
            return "Spreuken";
        }
        if (line.toLowerCase().startsWith("pred")) {
            return "Prediker";
        }
        if (line.toLowerCase().startsWith("hoog")) {
            return "Hooglied";
        }
        if (line.toLowerCase().startsWith("jesa")) {
            return "Jesaja";
        }
        if (line.toLowerCase().startsWith("jere")) {
            return "Jeremia";
        }
        if (line.toLowerCase().startsWith("klaa")) {
            return "Klaagliederen";
        }
        if (line.toLowerCase().startsWith("ezec")) {
            return "Ezechiel";
        }
        if (line.toLowerCase().startsWith("dani")) {
            return "Daniel";
        }
        if (line.toLowerCase().startsWith("hose")) {
            return "Hosea";
        }
        if (line.toLowerCase().startsWith("joel")) {
            return "Joel";
        }
        if (line.toLowerCase().startsWith("amos")) {
            return "Amos";
        }
        if (line.toLowerCase().startsWith("obad")) {
            return "Obadja";
        }
        if (line.toLowerCase().startsWith("jona")) {
            return "Jona";
        }
        if (line.toLowerCase().startsWith("mich")) {
            return "Micha";
        }
        if (line.toLowerCase().startsWith("nahu")) {
            return "Nahum";
        }
        if (line.toLowerCase().startsWith("haba")) {
            return "Habakuk";
        }
        if (line.toLowerCase().startsWith("sefa")) {
            return "Sefanja";
        }
        if (line.toLowerCase().startsWith("hagg")) {
            return "Haggai";
        }
        if (line.toLowerCase().startsWith("zach")) {
            return "Zacharia";
        }
        if (line.toLowerCase().startsWith("male")) {
            return "Maleachi";
        }
        if (line.toLowerCase().startsWith("matte")) {
            return "Matteus";
        }
        if (line.toLowerCase().startsWith("marcu")) {
            return "Marcus";
        }
        if (line.toLowerCase().startsWith("lucas")) {
            return "Lucas";
        }
        if (line.toLowerCase().startsWith("johan")) {
            return "Johannes";
        }
        if (line.toLowerCase().startsWith("hande")) {
            return "Handelingen";
        }
        if (line.toLowerCase().startsWith("romei")) {
            return "Romeinen";
        }
        if (line.toLowerCase().matches("^1 ?kor.*")) {
            return "1 Korintiers";
        }
        if (line.toLowerCase().matches("^2 ?kor.*")) {
            return "2 Korintiers";
        }
        if (line.toLowerCase().startsWith("galat")) {
            return "Galaten";
        }
        if (line.toLowerCase().startsWith("efezi")) {
            return "Efeziers";
        }
        if (line.toLowerCase().startsWith("filip")) {
            return "Filippenzen";
        }
        if (line.toLowerCase().startsWith("kolos")) {
            return "Kolossenzen";
        }
        if (line.toLowerCase().matches("^1 ?tes.*")) {
            return "1 Tessalonicenzen";
        }
        if (line.toLowerCase().matches("^2 ?tes.*")) {
            return "2 Tessalonicenzen";
        }
        if (line.toLowerCase().matches("^1 ?tim.*")) {
            return "1 Timoteus";
        }
        if (line.toLowerCase().matches("^2 ?tim.*")) {
            return "2 Timoteus";
        }
        if (line.toLowerCase().startsWith("titus")) {
            return "Titus";
        }
        if (line.toLowerCase().startsWith("filem")) {
            return "Filemon";
        }
        if (line.toLowerCase().startsWith("hebre")) {
            return "Hebreeen";
        }
        if (line.toLowerCase().startsWith("jakob")) {
            return "Jakobus";
        }
        if (line.toLowerCase().matches("^1 ?pet.*")) {
            return "1 Petrus";
        }
        if (line.toLowerCase().matches("^2 ?pet.*")) {
            return "2 Petrus";
        }
        if (line.toLowerCase().matches("^1 ?joh.*")) {
            return "1 Johannes";
        }
        if (line.toLowerCase().matches("^2 ?joh.*")) {
            return "2 Johannes";
        }
        if (line.toLowerCase().matches("^3 ?joh.*")) {
            return "3 Johannes";
        }
        if (line.toLowerCase().startsWith("judas")) {
            return "Judas";
        }
        if (line.toLowerCase().startsWith("openb")) {
            return "Openbaring";
        }
        throw new BibleException("Bijbelboek niet gevonden in liturgieregel: " + line);
    }

    public static int getChapterFromLine(String line) {
        if (line.contains(":")) {
            String s = StringUtils.substringBefore(line, ":");
            return Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfterLast(s, " "), ":").trim());
        } else {
            if (line.contains("(")) {
                return Integer.parseInt(StringUtils.substringBetween(line, " ", "(").trim());
            } else {
                return Integer.parseInt(StringUtils.substringAfter(line, " ").trim());
            }
        }
    }

    public static int getStartVerseFromLine(String line) {
        if (line.contains(":")) {
            if (line.contains("-")) {
                return Integer.parseInt(StringUtils.substringBetween(line, ":", "-").trim());
            } else {
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, ":", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
                }
            }
        } else {
            return 0;
        }
    }

    public static int getEndVerseFromLine(String line) {
        if (line.contains(":")) {
            if (line.contains("-")) {
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, "-", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfterLast(line, "-").trim());
                }
            } else {
                return Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
            }
        } else {
            return 999;
        }
    }

}
