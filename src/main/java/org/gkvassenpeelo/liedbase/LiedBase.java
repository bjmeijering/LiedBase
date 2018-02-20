package org.gkvassenpeelo.liedbase;

import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.GUI.LiedBaseView;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachineException;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachineException;

/**
 * TODO: voetnoten zoals in markus 15:28 TODO: log files weglaten
 * 
 * @author hdo20043
 *
 */
public class LiedBase {

	static final Logger logger = Logger.getLogger(LiedBase.class);

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException,
			SlideMachineException, PaperMachineException {

		// // Configure logger
		BasicConfigurator.configure();

		LiedBaseController controller = new LiedBaseController();
		LiedBaseView view = new LiedBaseView(controller);

		view.initialize();
		view.setVisible(true);

	}
}
