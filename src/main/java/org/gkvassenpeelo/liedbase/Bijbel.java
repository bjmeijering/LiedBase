package org.gkvassenpeelo.liedbase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Bijbel {

    private List<String> cookies;
    private HttpsURLConnection conn;

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0";
    private static String ENCODING = "UTF-8";

    public static void main(String[] args) throws Exception {

        String debijbel = "https://www.debijbel.nl/bijbel/zoeken/vertaling/johannes%2012";

        Bijbel http = new Bijbel();

        // make sure cookies is turn on
        CookieHandler.setDefault(new CookieManager());

        // get the page
        String result = http.GetPageContent(debijbel);
        
//        extractBibleTextPart(result);
    }

    protected String extractBibleTextPart(String result) {
        
        Document doc = Jsoup.parse(result, ENCODING );
        
        Elements bibletext = doc.select("div.bibletext");
        
        Elements paragraphs = bibletext.select("p.p");
        
        return paragraphs.toString();
    }

    private String GetPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.addRequestProperty(
                "Cookie",
                "_ga=GA1.2.276380152.1413559522; nbg_ecmgt_status=implicitconsent; PHPSESSID=dphegcqi6gljj35m157loe3pc0; auth_key=ecdfe1f8379ef2480e639e5e382901bd".split(";", 1)[0]);

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

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));

        return response.toString();

    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

}
