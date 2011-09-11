package gov.usgs.dismodel.help;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AboutDismodel extends JFrame {

    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    AboutDismodel frame = new AboutDismodel();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the frame.
     */
    public AboutDismodel() {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	setBounds(100, 100, 445, 321);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	
	JLabel lblUsgsDismodel = new JLabel("USGS Dismodel");
	lblUsgsDismodel.setFont(new Font("Tahoma", Font.PLAIN, 50));
	
	JTextArea txtrDevelopedByThe = new JTextArea();
	txtrDevelopedByThe.setFont(new Font("Monospaced", Font.PLAIN, 9));
	txtrDevelopedByThe.setEnabled(false);
	txtrDevelopedByThe.setEditable(false);
	txtrDevelopedByThe.setText("Developed by the US Geological Survery 2011 by:\r\n  Peter Cervelli, Jessica Murray-Moraleda,Maurizio Battaglia,\r\n  David McManamom, Chi Lam, Chris Forden, Cydric Lopez, Wan Yi Tam\r\nwith tremendous help from:\r\n  The NASA World Wind Team\r\nand using open source code projects:\r\n  NASA World Wind\r\n  Jzy3D\r\n  ojAlgo\r\n  US Forest Service Optimization Pack\r\n  Parallel Colt");
	
	JButton btnOk = new JButton("OK");
	btnOk.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
		    dispose();
		}
	});
	GroupLayout gl_contentPane = new GroupLayout(contentPane);
	gl_contentPane.setHorizontalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
			.addGroup(gl_contentPane.createSequentialGroup()
				.addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addComponent(txtrDevelopedByThe, GroupLayout.PREFERRED_SIZE, 389, GroupLayout.PREFERRED_SIZE)
					.addComponent(lblUsgsDismodel))
				.addContainerGap(20, Short.MAX_VALUE))
			.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
				.addContainerGap(372, Short.MAX_VALUE)
				.addComponent(btnOk))
	);
	gl_contentPane.setVerticalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
			.addGroup(gl_contentPane.createSequentialGroup()
				.addContainerGap()
				.addComponent(lblUsgsDismodel)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(txtrDevelopedByThe, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
				.addComponent(btnOk))
	);
	contentPane.setLayout(gl_contentPane);
    }
}
