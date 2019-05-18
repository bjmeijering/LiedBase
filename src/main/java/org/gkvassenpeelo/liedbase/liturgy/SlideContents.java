package org.gkvassenpeelo.liedbase.liturgy;

public abstract class SlideContents {

    private String header;

    private String body;

    private Type type;

    public enum Type {
        psalm, gezang, lied, levenslied, opwekking, gathering, prair, blank, bible_start, bible_continued
    };

    public SlideContents(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public SlideContents(String header, String body, Type type) {
        this.header = header;
        this.body = body;
        this.type = type;
    }

    public SlideContents() {
    }

    public String getHeader() {
        return header.substring(0, 1).toUpperCase() + header.substring(1);
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
