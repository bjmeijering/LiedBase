package org.gkvassenpeelo.liedbase;

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
		PaperMachine paperMachine = new PaperMachine(model.getLiturgy());
		paperMachine.createDocument();
		paperMachine.save();
		return null;
	}

	@Override
	public void done() {
		view.docxBuildStop();
	}
}
