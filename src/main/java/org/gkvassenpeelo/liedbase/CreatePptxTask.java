package org.gkvassenpeelo.liedbase;

import javax.swing.SwingWorker;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;

public class CreatePptxTask extends SwingWorker<Void, Void> {

	LiturgyModel model;
	LiedBaseView view;

	public CreatePptxTask(LiturgyModel model, LiedBaseView view) {
		this.model = model;
		this.view = view;
	}

	@Override
	protected Void doInBackground() {
		SlideMachine slideMachine;
		try {
			slideMachine = new SlideMachine(model);
			slideMachine.createSlides();
			slideMachine.save();
		} catch (SlideMachineException e) {
			view.writeLineToConsole(e.getMessage());
		} catch (Docx4JException e) {
			view.writeLineToConsole(e.getMessage());
		}
		return null;
	}

	@Override
	public void done() {
		view.pptxBuildStop();
	}
}
