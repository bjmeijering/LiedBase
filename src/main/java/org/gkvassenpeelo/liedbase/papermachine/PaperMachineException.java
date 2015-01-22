package org.gkvassenpeelo.liedbase.papermachine;

@SuppressWarnings("serial")
public class PaperMachineException extends Exception {

    public PaperMachineException(String message) {
        super(message);
    }

    public PaperMachineException(String message, Exception e) {
        super(message, e);
    }
}
