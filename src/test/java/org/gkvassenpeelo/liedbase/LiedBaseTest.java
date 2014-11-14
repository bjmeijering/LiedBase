package org.gkvassenpeelo.liedbase;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.Before;
import org.junit.Test;

public class LiedBaseTest {

    LiedBase lb = new LiedBase();

    @Before
    public void setUp() throws Exception {
        lb.setTargetFile(new File("target/Presentatie.pptx"));
    }

    @Test
    public void liedBasetest() throws LiedBaseError, Docx4JException {

        lb.setSourceFile(new File("src/test/resources/liturgie.txt"));

        lb.parseLiturgyScript();
        lb.createSlides();
        lb.save();

    }

    @Test
    public void getTimeFromLineTest() {
        assertEquals("", lb.getTimeFromLine(""));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00,"));
        assertEquals("15:00", lb.getTimeFromLine("einde morgendienst: 15:00, dominee"));
        assertEquals("dominee l.l", lb.getNextVicarFromLine("einde morgendienst: 15:00, dominee l.l"));
    }

    @Test
    public void downloadBible() throws MalformedURLException, IOException {

        String url = "https://www.debijbel.nl/bijbel/zoeken/vertaling/johannes%2012";
        String charset = "UTF-8";
        String param1 = "value1";
        String param2 = "value2";
        // ...

        String query = String.format("param1=%s&param2=%s", URLEncoder.encode(param1, charset), URLEncoder.encode(param2, charset));

        // First set the default cookie manager.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        URLConnection connection = new URL(url + "?" + query).openConnection();
        
        connection.setRequestProperty("Accept-Charset", charset);
        
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
        InputStream response = connection.getInputStream();

        int status = ((HttpURLConnection) connection).getResponseCode();

        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            System.out.println(header.getKey() + "=" + header.getValue());
        }

        String contentType = connection.getHeaderField("Content-Type");
        charset = null;

        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                charset = param.split("=", 2)[1];
                break;
            }
        }

//        if (charset != null) {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset))) {
//                for (String line; (line = reader.readLine()) != null;) {
//                    System.out.println(line);
//                }
//            }
//        }
//        else {
//            System.out.println("its binary...");
//        }
    }

}
