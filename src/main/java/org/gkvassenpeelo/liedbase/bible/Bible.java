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

        book = String.format("%s%s", book.substring(0, 1).toUpperCase(), book.toLowerCase().substring(1));

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

        for (Element part : parts) {

            int currentStartVerse = -1;
            int currentEndVerse = 1000;

            if (!"p".equals(part.className()) && !"s".equals(part.className())) {
                continue;
            }

            // h3 header
            if (part.attributes().get("class").equals("s")) {
                if (currentStartVerse >= fromVerse && currentEndVerse <= toVerse) {
                    if (bp.size() == 0) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, part.text() + LINE_END));
                    } else {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, LINE_END + part.text() + LINE_END));
                    }
                }
            }

            for (Element verse : part.select("span.verse")) {
                String verseId = verse.select("sup").text();
                if (verseId.contains("-")) {
                    currentStartVerse = Integer.parseInt(StringUtils.substringBefore(verseId, "-"));
                    currentEndVerse = Integer.parseInt(StringUtils.substringAfter(verseId, "-"));
                } else {
                    currentStartVerse = currentEndVerse = Integer.parseInt(verseId);
                }
                if (currentStartVerse >= fromVerse && currentEndVerse <= toVerse) {
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.superScript, verse.select("sup").first().text().trim()));
                    verse.select("sup").first().html("");
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, verse.text().trim()));
                }

                // stop iterating verses
                if (currentStartVerse > toVerse) {
                    break;
                }
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
        conn.addRequestProperty("Cookie",
                "_ga=GA1.2.276380152.1413559522; nbg_ecmgt_status=implicitconsent; auth_key=15ca36449ab755a25f3be9f4785ffbfe; PHPSESSID=dphegcqi6gljj35m157loe3pc0; _gat=1".split(
                        ";", 1)[0]);

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
        if (line.toLowerCase().startsWith("Levi")) {
            return "Leviticus";
        }
        if (line.toLowerCase().startsWith("Nume")) {
            return "Numeri";
        }
        if (line.toLowerCase().startsWith("Deut")) {
            return "Deuteronomium";
        }
        if (line.toLowerCase().startsWith("Jozu")) {
            return "Jozua";
        }
        if (line.toLowerCase().startsWith("Rech")) {
            return "Rechters";
        }
        if (line.toLowerCase().startsWith("Ruth")) {
            return "Ruth";
        }
        if (line.toLowerCase().matches("^1 ?sam.*")) {
            return "1+Samuel";
        }
        if (line.toLowerCase().matches("^2 ?sam.*")) {
            return "2+Samuel";
        }
        if (line.toLowerCase().matches("^1 ?kon.*")) {
            return "1+Koningen";
        }
        if (line.toLowerCase().matches("^2 ?kon.*")) {
            return "2+Koningen";
        }
        if (line.toLowerCase().matches("^1 ?kro.*")) {
            return "1+Kronieken";
        }
        if (line.toLowerCase().matches("^2 ?kro.*")) {
            return "2+Kronieken";
        }
        if (line.toLowerCase().startsWith("Ezra")) {
            return "Ezra";
        }
        if (line.toLowerCase().startsWith("Nehe")) {
            return "Nehemia";
        }
        if (line.toLowerCase().startsWith("Este")) {
            return "Ester";
        }
        if (line.toLowerCase().startsWith("Job")) {
            return "Job";
        }
        if (line.toLowerCase().startsWith("Psal")) {
            return "Psalmen";
        }
        if (line.toLowerCase().startsWith("Spre")) {
            return "Spreuken";
        }
        if (line.toLowerCase().startsWith("Pred")) {
            return "Prediker";
        }
        if (line.toLowerCase().startsWith("Hoog")) {
            return "Hooglied";
        }
        if (line.toLowerCase().startsWith("Jesa")) {
            return "Jesaja";
        }
        if (line.toLowerCase().startsWith("Jere")) {
            return "Jeremia";
        }
        if (line.toLowerCase().startsWith("Klaa")) {
            return "Klaagliederen";
        }
        if (line.toLowerCase().startsWith("Ezec")) {
            return "Ezechiel";
        }
        if (line.toLowerCase().startsWith("Dani")) {
            return "Daniel";
        }
        if (line.toLowerCase().startsWith("Hose")) {
            return "Hosea";
        }
        if (line.toLowerCase().startsWith("Joel")) {
            return "Joel";
        }
        if (line.toLowerCase().startsWith("Amos")) {
            return "Amos";
        }
        if (line.toLowerCase().startsWith("Obad")) {
            return "Obadja";
        }
        if (line.toLowerCase().startsWith("Jona")) {
            return "Jona";
        }
        if (line.toLowerCase().startsWith("Mich")) {
            return "Micha";
        }
        if (line.toLowerCase().startsWith("Nahu")) {
            return "Nahum";
        }
        if (line.toLowerCase().startsWith("Haba")) {
            return "Habakuk";
        }
        if (line.toLowerCase().startsWith("Sefa")) {
            return "Sefanja";
        }
        if (line.toLowerCase().startsWith("Hagg")) {
            return "Haggai";
        }
        if (line.toLowerCase().startsWith("Zach")) {
            return "Zacharia";
        }
        if (line.toLowerCase().startsWith("Male")) {
            return "Maleachi";
        }
        throw new BibleException("Bijbelboek niet gevonden in regel: " + line);
    }

    public static int getChapterFromLine(String line) {
        if (line.contains(":")) {
            return Integer.parseInt(StringUtils.substringBetween(line, " ", ":").trim());
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
