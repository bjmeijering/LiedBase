package org.gkvassenpeelo.liedbase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.swing.SwingWorker;

import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyItem;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;

public class CreatePptxTask extends SwingWorker<Void, Void> {

	LiedBaseView view;

	private List<LiturgyItem> items;

	public CreatePptxTask(List<LiturgyItem> items, LiedBaseView view) {
		this.items = items;
		this.view = view;
	}

	@Override
	protected Void doInBackground() {
		SlideMachine slideMachine;
		try {
			slideMachine = new SlideMachine(items);
			slideMachine.createSlides();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			view.writeLineToConsole("Oeps... Er is iets fout gegaan.");
			view.writeLineToConsole(e.getMessage());
			view.writeLineToConsole(sw.toString());
		}
		return null;
	}

	@Override
	public void done() {
		view.pptxBuildStop();
	}
}
