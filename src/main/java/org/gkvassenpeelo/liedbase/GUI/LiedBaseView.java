package org.gkvassenpeelo.liedbase.GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.gkvassenpeelo.liedbase.LiedBaseController;
import net.miginfocom.swing.MigLayout;

public class LiedBaseView {

	private JFrame frame;
	private LiedBaseController controller;

	/**
	 * Create the application.
	 * 
	 * 
	 * 
	 * @param controller
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public LiedBaseView(LiedBaseController controller) {
		this.controller = controller;
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
		frame.setBounds(100, 100, 400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("wrap 2", "[][grow]", "[grow][grow][grow][grow]"));

		JTextArea taLiturgy = new JTextArea();
		frame.getContentPane().add(taLiturgy, "w 300::800,h 400::700,span 1 3");

		JButton btnLiturgyControleren = new JButton("Liturgy controleren");
		frame.getContentPane().add(btnLiturgyControleren, "");
		btnLiturgyControleren.setActionCommand("checkLiturgy");

		JButton btnGeneratePptx = new JButton();
		frame.getContentPane().add(btnGeneratePptx, "");
		btnGeneratePptx.setAction(controller);
		btnGeneratePptx.setText("Presentatie maken");
		btnGeneratePptx.setActionCommand("generatePptx");

		JButton btnGenerateDocx = new JButton();
		frame.getContentPane().add(btnGenerateDocx, "");
		btnGenerateDocx.setAction(controller);
		btnGenerateDocx.setText("Boekje maken");
		btnGenerateDocx.setActionCommand("generateDocx");
		btnGenerateDocx.setFont(new Font("Tahoma", Font.PLAIN, 13));

		JTextArea console = new JTextArea();
		frame.getContentPane().add(console, "span,w 500::1000,h 100::, growx");
	}

	public void setVisible(boolean b) {
		frame.setVisible(true);
		frame.pack();
	}
}
