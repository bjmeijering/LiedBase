package org.gkvassenpeelo.liedbase.GUI;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

import org.gkvassenpeelo.liedbase.LiedBaseController;

public class LiedBaseView {

	private JFrame frame;
	private LiedBaseController controller;

	// buttons
	JButton btnGeneratePptx = new JButton();
	JButton btnGenerateDocx = new JButton();

	JTextArea taLiturgy = new JTextArea();
	JTextArea console = new JTextArea();

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
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("wrap 2", "[][grow]", "[grow][grow][grow][grow]"));

		JScrollPane taScrollPane = new JScrollPane(taLiturgy, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(taScrollPane, "w 300::800,h 400::700,span 1 2");

		frame.getContentPane().add(btnGeneratePptx, "");
		btnGeneratePptx.setAction(controller);
		btnGeneratePptx.setText("Presentatie maken");
		btnGeneratePptx.setActionCommand("generatePptx");

		frame.getContentPane().add(btnGenerateDocx, "");
		btnGenerateDocx.setAction(controller);
		btnGenerateDocx.setText("Boekje maken");
		btnGenerateDocx.setActionCommand("generateDocx");
		btnGenerateDocx.setFont(new Font("Tahoma", Font.PLAIN, 13));

		console.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(consoleScrollPane, "spanx ,growx,width 500::1000,hmin 100,aligny baseline");
	}

	public void writeLineToConsole(String message) {
		console.append(message + System.getProperty("line.separator"));
	}

	public void setVisible(boolean b) {
		frame.setVisible(true);
		frame.pack();
	}
	
	public String getLiturgyText() {
		return taLiturgy.getText();
	}
}
