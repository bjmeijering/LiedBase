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
import org.gkvassenpeelo.liedbase.songbook.SongBookException;

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
				LiturgyParseResult result = model.parseLiturgyScript(view.getLiturgyText());
				if (!result.hasErrors()) {
					slideMachine = new SlideMachine(model);
					slideMachine.createSlides();
					slideMachine.save();
				} else {
					for (String message : result.getErrors()) {
						view.writeLineToConsole(message);
					}
				}
			} catch (Docx4JException e) {
				view.writeLineToConsole(e.getMessage());
			} catch (SlideMachineException e) {
				view.writeLineToConsole(e.getMessage());
			} catch (BibleException e) {
				view.writeLineToConsole(e.getMessage());
			} catch (SongBookException e) {
				view.writeLineToConsole(e.getMessage());
			}
		}

		if ("generateDocx".equals(event.getActionCommand())) {
			try {
				LiturgyParseResult result = model.parseLiturgyScript(view.getLiturgyText());
				if (!result.hasErrors()) {
					paperMachine = new PaperMachine(model.getLiturgy());
					paperMachine.createDocument();
					paperMachine.save();
				} else {
					for (String message : result.getErrors()) {
						view.writeLineToConsole(message);
					}
				}
			} catch (PaperMachineException e) {
				view.writeLineToConsole(e.getLocalizedMessage());
			} catch (BibleException e) {
				view.writeLineToConsole(e.getMessage());
			} catch (SongBookException e) {
				view.writeLineToConsole(e.getMessage());
			}
		}

	}

	public void setLiedBaseView(LiedBaseView view) {
		this.view = view;
	}

}
