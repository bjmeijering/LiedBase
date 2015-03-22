package org.gkvassenpeelo.liedbase;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyParseResult;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachine;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachineException;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;

@SuppressWarnings("serial")
public class LiedBaseController extends AbstractAction {

	private LiturgyModel model;
	private LiedBaseView view;
	private SlideMachine slideMachine;
	private PaperMachine paperMachine;

	public LiedBaseController(LiturgyModel model) {
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if ("generatePptx".equals(event.getActionCommand())) {
			try {
				slideMachine = new SlideMachine(model);
				slideMachine.createSlides();
				slideMachine.save();
			} catch (Docx4JException e) {
				e.printStackTrace();
			} catch (SlideMachineException e) {
				e.printStackTrace();
			}
		}

		if ("checkLiturgy".equals(event.getActionCommand())) {
			try {
				LiturgyParseResult result = model.parseLiturgyScript(view.getLiturgyText());
				if (!result.hasErrors()) {
					view.enableGenerateButtons();
				} else {
					for (String message : result.getErrors()) {
						view.writeLineToConsole(message);
					}
				}
			} catch (BibleException e) {
				e.printStackTrace();
			}
		}

		if ("generateDocx".equals(event.getActionCommand())) {
			try {
				paperMachine = new PaperMachine(model.getLiturgy());
				paperMachine.createDocument();
				paperMachine.save();
			} catch (PaperMachineException e) {
				e.printStackTrace();
			}
		}

	}

	public void setLiedBaseView(LiedBaseView view) {
		this.view = view;
	}

}
