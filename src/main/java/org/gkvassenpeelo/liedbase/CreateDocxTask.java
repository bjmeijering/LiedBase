package org.gkvassenpeelo.liedbase;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.SwingWorker;

import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachine;

public class CreateDocxTask extends SwingWorker<Void, Void> {

	LiturgyModel model;
	LiedBaseView view;

	public CreateDocxTask(LiturgyModel model, LiedBaseView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	protected Void doInBackground() throws Exception {
//		try {
//			PaperMachine paperMachine = new PaperMachine(model.getLiturgy());
//			paperMachine.createDocument();
//			paperMachine.save();
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			e.printStackTrace(pw);
//			view.writeLineToConsole("Oeps... Er is iets fout gegaan.");
//			if (e.getMessage() != null) {
//				view.writeLineToConsole(e.getMessage());
//			}
//			view.writeLineToConsole(sw.toString());
//		}
		return null;
	}

	@Override
	public void done() {
		view.docxBuildStop();
	}
}
