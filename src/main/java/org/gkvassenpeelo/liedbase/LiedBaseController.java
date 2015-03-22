package org.gkvassenpeelo.liedbase;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyBuilder;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;

@SuppressWarnings("serial")
public class LiedBaseController extends AbstractAction {

    private LiturgyBuilder lb;

    @Override
    public void actionPerformed(ActionEvent e) {
        SlideMachine slideMachine;
        try {
            slideMachine = new SlideMachine(lb.getLiturgy(), lb.getLiturgyView());
            slideMachine.createSlides();
            slideMachine.save();
        } catch (SlideMachineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Docx4JException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void setLiturgyBuilder(LiturgyBuilder lb) {
        this.lb = lb;
    }

}
