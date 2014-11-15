package org.gkvassenpeelo.liedbase.bible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Bible {

    private static String ENCODING = "UTF-8";

    private String url = "https://www.debijbel.nl/bijbel/zoeken/%s/%s+%s";

    public Bible() {

    }

    public String extractBibleChapterFromHtml(String result) {

        Document doc = Jsoup.parse(result, ENCODING);

        return doc.select("div.bibletext").toString();

        // Elements bibletext = doc.select("div.bibletext");

        // Elements paragraphs = bibletext.select("p.p");
        //
        // String chapter = paragraphs.select("span.chapterStart").text();
        //
        // System.out.print(chapter);
        //
        // for (Element paragraph : paragraphs) {
        // for (Element verse : paragraph.select("span.verse")) {
        // System.out.print(" " + verse.select("sup").first().text() + " ");
        // verse.select("sup").first().html("");
        // System.out.print(verse.text());
        // }
        // }
        //
        // return paragraphs.toString();
    }

    public void downloadAndSaveBibleBook(String book, String maxChapter, String translation) throws Exception {

        File f = new File(translation + "/" + book + ".dat");
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
                "_ga=GA1.2.276380152.1413559522; nbg_ecmgt_status=implicitconsent; auth_key=15ca36449ab755a25f3be9f4785ffbfe; PHPSESSID=dphegcqi6gljj35m157loe3pc0; _gat=1".split(";", 1)[0]);
                
        
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

}
