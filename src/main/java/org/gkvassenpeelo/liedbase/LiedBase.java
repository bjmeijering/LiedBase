package org.gkvassenpeelo.liedbase;

import javax.swing.UnsupportedLookAndFeelException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.GUI.LiedbaseWindow;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyBuilder;

/**
 * TODO: Koppen met komma (na jesaja 53: 12)
 * 
 * @author hdo20043
 *
 */
public class LiedBase {
    
    LiedBaseController lbc = new LiedBaseController();

    public static void main(String[] args) throws Docx4JException, BibleException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {

        new LiedbaseWindow();

        LiturgyBuilder lb = new LiturgyBuilder();
        lb.parseLiturgyScript();
        
        
        
        // PaperMachine pm = new PaperMachine(lb.getLiturgy());
        // pm.createDocument();
        // pm.save();
    }
}
