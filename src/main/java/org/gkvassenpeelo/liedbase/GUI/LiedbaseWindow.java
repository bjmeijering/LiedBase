package org.gkvassenpeelo.liedbase.GUI;

import java.awt.BorderLayout;
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
import javax.swing.AbstractAction;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class LiedbaseWindow {

    private JFrame frame;
    private final Action action = new SwingAction();
    JTextArea console;

    /**
     * Create the application.
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    public LiedbaseWindow() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    private void initialize() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        
        frame = new JFrame();
        frame.setBounds(100, 100, 520, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane();
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        JEditorPane dtrpnLiturgie = new JEditorPane();
        splitPane.setLeftComponent(dtrpnLiturgie);
        
        JTextPane txtpnUitleg = new JTextPane();
        txtpnUitleg.setText("Uitleg");
        txtpnUitleg.setEditable(false);
        splitPane.setRightComponent(txtpnUitleg);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{504, 0};
        gbl_panel.rowHeights = new int[]{23, 23, 23, 0};
        gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        JButton btnGeneratePptx = new JButton("Presentatie maken");
        btnGeneratePptx.setAction(action);
        GridBagConstraints gbc_btnGeneratePptx = new GridBagConstraints();
        gbc_btnGeneratePptx.fill = GridBagConstraints.BOTH;
        gbc_btnGeneratePptx.insets = new Insets(0, 0, 0, 0);
        gbc_btnGeneratePptx.gridx = 0;
        gbc_btnGeneratePptx.gridy = 0;
        panel.add(btnGeneratePptx, gbc_btnGeneratePptx);
        
        JButton btnGenerateDocx = new JButton("Boekje maken");
        GridBagConstraints gbc_btnGenerateDocx = new GridBagConstraints();
        gbc_btnGenerateDocx.fill = GridBagConstraints.BOTH;
        gbc_btnGenerateDocx.insets = new Insets(0, 0, 0, 0);
        gbc_btnGenerateDocx.gridx = 0;
        gbc_btnGenerateDocx.gridy = 1;
        panel.add(btnGenerateDocx, gbc_btnGenerateDocx);
        
        console = new JTextArea();
        console.setRows(3);
        console.setEditable(false);
        GridBagConstraints gbc_console = new GridBagConstraints();
        gbc_console.fill = GridBagConstraints.BOTH;
        gbc_console.gridx = 0;
        gbc_console.gridy = 2;
        panel.add(console, gbc_console);
    }

    public void setVisible(boolean b) {
        frame.setVisible(true);
    }

    @SuppressWarnings("serial")
    private class SwingAction extends AbstractAction {
        public SwingAction() {
            putValue(NAME, "SwingAction");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }
        public void actionPerformed(ActionEvent e) {
            console.setText("wheee!");
        }
    }
}
