package org.gkvassenpeelo.liedbase.liturgy;

public abstract class SlideContents {

    private String header;

    private String body;
    
    public enum Type {
        psalm, gezang, lied, levenslied, opwekking, gathering, prair, blank, bible_start, bible_continued
    };

    public SlideContents(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public SlideContents() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
}
