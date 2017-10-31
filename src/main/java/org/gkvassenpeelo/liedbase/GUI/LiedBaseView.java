package org.gkvassenpeelo.liedbase.GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.gkvassenpeelo.liedbase.LiedBaseController;

public class LiedBaseView {

	private JFrame frame;
	private LiedBaseController controller;

	// buttons
	private JButton btnGeneratePptx = new JButton();
	private JButton btnGenerateDocx = new JButton();

	private JTextArea taLiturgy = new JTextArea();
	private JTextArea console = new JTextArea();

	private JProgressBar pptxProgressBar;
	private JProgressBar docxProgressBar;

	/**
	 * Create the view of the application.
	 * 
	 * @param controller
	 */
	public LiedBaseView(LiedBaseController controller) {
		this.controller = controller;
		controller.setLiedBaseView(this);
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @wbp.parser.entryPoint
	 */
	public void initialize() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

		frame = new JFrame();
		frame.addWindowListener(new MyWindowListener());
		frame.setBounds(100, 100, 1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("wrap 3", "[][grow]", "[grow][grow][grow][grow]"));

		JPanel liturgyPartButtons = new JPanel();
		liturgyPartButtons.setLayout(new MigLayout("wrap 1", "[]", "[]"));
		frame.getContentPane().add(liturgyPartButtons, "w 180::800,h 400::700,span 1 2");
		liturgyPartButtons.add(createLiturgyButton("Agenda", "agenda"));
		liturgyPartButtons.add(createLiturgyButton("Kerkschoonmaak", "schoonmaak"));
		liturgyPartButtons.add(createLiturgyButton("Welkom", "welkom: [naam voorganger]"));
		liturgyPartButtons.add(createLiturgyButton("Votum", "votum"));
		liturgyPartButtons.add(createLiturgyButton("Psalm", "psalm 1:1,2"));
		liturgyPartButtons.add(createLiturgyButton("Gezang", "gezang 1:1,2"));
		liturgyPartButtons.add(createLiturgyButton("Lied", "liedboek 1:1,2"));
		liturgyPartButtons.add(createLiturgyButton("Opwekking", "opwekking 1"));
		liturgyPartButtons.add(createLiturgyButton("Levenslied", "levenslied 1:1,2"));
		liturgyPartButtons.add(createLiturgyButton("Schriftlezing", "genesis 1:1-2 (BGT)"));
		liturgyPartButtons.add(createLiturgyButton("Gebed", "gebed"));
		liturgyPartButtons.add(createLiturgyButton("Preek", "preek"));
		liturgyPartButtons.add(createLiturgyButton("Collecte", "collecte: doel 1, doel 2"));
		liturgyPartButtons.add(createLiturgyButton("Amen/Zegen", "amen"));
		liturgyPartButtons.add(createLiturgyButton("Einde morgendienst", "einde morgendienst: [aanvangstijd middagdienst], [naam voorganger]"));
		liturgyPartButtons.add(createLiturgyButton("Einde middagdienst", "einde middagdienst"));
		liturgyPartButtons.add(createLiturgyButton("Leeg met logo", "leeg met logo"));

		JScrollPane taScrollPane = new JScrollPane(taLiturgy, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(taScrollPane, "w 400::1920,h 580::1080,span 1 2");

		frame.getContentPane().add(btnGeneratePptx, "flowx");
		btnGeneratePptx.setAction(controller);
		btnGeneratePptx.setText("Presentatie maken");
		btnGeneratePptx.setActionCommand("generatePptx");
		btnGeneratePptx.setPreferredSize(new Dimension(140, 20));

		frame.getContentPane().add(btnGenerateDocx, "");
		btnGenerateDocx.setAction(controller);
		btnGenerateDocx.setText("Boekje maken");
		btnGenerateDocx.setActionCommand("generateDocx");
		btnGenerateDocx.setPreferredSize(new Dimension(140, 20));

		console.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(consoleScrollPane, "cell 0 2, spanx, growx, width 500::1000, hmin 100, aligny baseline");

		pptxProgressBar = new JProgressBar();
		frame.getContentPane().add(pptxProgressBar, "cell 2 0");

		docxProgressBar = new JProgressBar();
		frame.getContentPane().add(docxProgressBar, "cell 2 1");
	}

	public void writeLineToConsole(String message) {
		console.append(message + System.getProperty("line.separator"));
	}

	public void pptxBuildStart() {
		btnGeneratePptx.setEnabled(false);
		pptxProgressBar.setIndeterminate(true);
	}

	public void pptxBuildStop() {
		btnGeneratePptx.setEnabled(true);
		pptxProgressBar.setIndeterminate(false);
	}

	public void docxBuildStart() {
		btnGenerateDocx.setEnabled(false);
		docxProgressBar.setIndeterminate(true);
	}

	public void docxBuildStop() {
		btnGenerateDocx.setEnabled(true);
		docxProgressBar.setIndeterminate(false);
	}

	public void setVisible(boolean b) {
		frame.setVisible(true);
		frame.pack();
	}

	public String getLiturgyText() {
		return taLiturgy.getText();
	}

	public void setLiturgy(String text) {
		taLiturgy.setText(text);
	}

	private JButton createLiturgyButton(String buttonText, String liturgyLine) {
		JButton button = new JButton();
		button.setAction(new LiturgyButtonAction(liturgyLine));
		button.setText(buttonText);
		button.setPreferredSize(new Dimension(160, 20));
		return button;
	}

	private class LiturgyButtonAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private String liturgyLine;

		public LiturgyButtonAction(String liturgyLine) {
			this.liturgyLine = liturgyLine;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int position = taLiturgy.getCaretPosition();
			taLiturgy.insert(liturgyLine, position);
			taLiturgy.requestFocus();
		}

	}

	private class MyWindowListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			try {
				FileUtils.writeStringToFile(new File("liturgie.txt"), taLiturgy.getText());
			} catch (IOException e1) {
				// noop
			}
			super.windowClosing(e);
		}

	}
}
