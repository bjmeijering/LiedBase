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

import org.gkvassenpeelo.slidemachine.model.BiblePartFragment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Bible {

    private static String ENCODING = "UTF-8";

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
            in.close();
        } catch (IOException e) {
            throw new BibleException(String.format("Boek %s in vertaling %s niet gevonden", book, translation));
        } catch (NullPointerException e) {
            throw new BibleException(String.format("Boek %s in vertaling %s niet gevonden", book, translation));
        }

        Element bibletext = doc.select("div[id=scroller]").get(chapter - 1);

        List<BiblePartFragment> bp = new ArrayList<BiblePartFragment>();

        Elements parts = bibletext.children();

        int currentVerse = 1;

        for (Element part : parts) {

            if (!"p".equals(part.className()) && !"s".equals(part.className())) {
                continue;
            }

            // h3 header
            if (part.attributes().get("class").equals("s")) {
                if (currentVerse >= fromVerse && currentVerse <= toVerse) {
                    if (bp.size() == 0) {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, part.text() + LINE_END));
                    } else {
                        bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, LINE_END + part.text() + LINE_END));
                    }
                }
            }

            for (Element verse : part.select("span.verse")) {
                if (currentVerse >= fromVerse && currentVerse <= toVerse) {
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.superScript, verse.select("sup").first().text().trim()));
                    verse.select("sup").first().html("");
                    bp.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, verse.text().trim()));
                }
                currentVerse++;
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
    
    public static String getTranslation(String line) throws BibleException {
        if(line.toLowerCase().endsWith("(nbv)")) {
            return "NBV";
        }
        if(line.toLowerCase().endsWith("(bgt)")) {
            return "BGT";
        }
        throw new BibleException("Onbekende vertaling in regel: " + line);
    }

    // for each bible book an if statement
    public static String getBibleBook(String line) throws BibleException {
        if (line.toLowerCase().startsWith("gen")) {
            return "Genisis";
        }
        throw new BibleException("Bijbelboek niet gevonden in regel: " + line);
    }

}
