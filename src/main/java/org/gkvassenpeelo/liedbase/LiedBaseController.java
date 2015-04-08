package org.gkvassenpeelo.liedbase;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyParseResult;
import org.gkvassenpeelo.liedbase.songbook.SongBookException;

@SuppressWarnings("serial")
public class LiedBaseController extends AbstractAction implements PropertyChangeListener {

	private LiturgyModel model;
	private LiedBaseView view;

	public LiedBaseController(LiturgyModel model) {
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if ("generatePptx".equals(event.getActionCommand())) {
			try {
				LiturgyParseResult result = model.parseLiturgyScript(view.getLiturgyText());
				if (!result.hasErrors()) {
					view.pptxBuildStart();
					SwingWorker<Void, Void> task = new CreatePptxTask(model, view);
					task.execute();
				} else {
					for (String message : result.getErrors()) {
						view.writeLineToConsole(message);
					}
				}
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
					view.docxBuildStart();
					SwingWorker<Void, Void> task = new CreateDocxTask(model, view);
					task.execute();
				} else {
					for (String message : result.getErrors()) {
						view.writeLineToConsole(message);
					}
				}
			} catch (BibleException e) {
				view.writeLineToConsole(e.getMessage());
			} catch (SongBookException e) {
				view.writeLineToConsole(e.getMessage());
			}
		}

	}

	public void setLiedBaseView(LiedBaseView view) {
		this.view = view;

		// on startup set the liturgy
		try {
			view.writeLineToConsole("Proberen om liturgie.txt te laden...");
			view.setLiturgy(FileUtils.readFileToString(new File("liturgie.txt")));
			view.writeLineToConsole("liturgie.txt geladen");
		} catch (IOException e) {
			view.writeLineToConsole("Laden van liturgie.txt is mislukt: " + e.getMessage());
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// noop
	}

}
