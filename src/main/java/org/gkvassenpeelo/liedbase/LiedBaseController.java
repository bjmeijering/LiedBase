package org.gkvassenpeelo.liedbase;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyItem;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyParseResult;
import org.gkvassenpeelo.liedbase.liturgy.Parser;

@SuppressWarnings("serial")
public class LiedBaseController extends AbstractAction implements PropertyChangeListener {

	private LiturgyModel model = new LiturgyModel();
	private LiedBaseView view;
	private Parser parser = new Parser();

	@Override
	public void actionPerformed(ActionEvent event) {

		if ("generatePptx".equals(event.getActionCommand())) {
			parser.setText(view.getLiturgyText());

			LiturgyParseResult result = null;
			try {
				result = parser.parseLiturgyScript();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<LiturgyItem> liturgyItems = result.getLiturgyItems();

			for (LiturgyItem item : liturgyItems) {
				item.loadContent();
				model.addLiturgyItem(item);
			}

			if (!result.hasErrors()) {
				view.pptxBuildStart();
				SwingWorker<Void, Void> task = new CreatePptxTask(model, view);
				task.execute();
			} else {
				for (String message : result.getErrors()) {
					view.writeLineToConsole(message);
				}
			}
		}

		// if ("generateDocx".equals(event.getActionCommand())) {
		// try {
		// LiturgyParseResult result =
		// model.parseLiturgyScript(view.getLiturgyText());
		// if (!result.hasErrors()) {
		// view.docxBuildStart();
		// SwingWorker<Void, Void> task = new CreateDocxTask(model, view);
		// task.execute();
		// } else {
		// for (String message : result.getErrors()) {
		// view.writeLineToConsole(message);
		// }
		// }
		// } catch (BibleException e) {
		// view.writeLineToConsole(e.getMessage());
		// } catch (SongBookException e) {
		// view.writeLineToConsole(e.getMessage());
		// }
		// }

	}

	public void setLiedBaseView(LiedBaseView view) {
		this.view = view;

		view.writeLineToConsole("Welkom bij LiedBase versie: 3.4");

		// on startup set the liturgy
		try {
			File liturgyFile = new File("liturgie.txt");
			view.setLiturgy(FileUtils.readFileToString(liturgyFile));
			view.writeLineToConsole("Liturgie is geladen vanuit: " + liturgyFile.getAbsolutePath());
		} catch (IOException e) {
			view.writeLineToConsole("Geen bestaande liturgie gevonden, klik op de knoppen links om te beginnen.");
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// noop
	}

}
