package org.gkvassenpeelo.liedbase;

import javax.swing.SwingWorker;

import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;

public class CreatePptxTask extends SwingWorker<Void, Void> {

	LiturgyModel model;
	LiedBaseView view;

	public CreatePptxTask(LiturgyModel model, LiedBaseView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	protected Void doInBackground() throws Exception {
		SlideMachine slideMachine = new SlideMachine(model);
		slideMachine.createSlides();
		slideMachine.save();
		return null;
	}

	@Override
	public void done() {
		view.pptxBuildStop();
	}
}
