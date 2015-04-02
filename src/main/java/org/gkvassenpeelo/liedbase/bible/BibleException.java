package org.gkvassenpeelo.liedbase.bible;

public class BibleException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public BibleException(String cause) {
    	super(cause);
    }

    public BibleException(String cause, Exception e) {
        super(cause, e);
    }
}
