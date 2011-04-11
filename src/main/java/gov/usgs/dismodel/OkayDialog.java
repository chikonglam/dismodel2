package gov.usgs.dismodel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * A dialog with an okay and cancel button.
 * 
 * @author dmcmanamon
 */
public class OkayDialog extends javax.swing.JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private JButton buttonCancel;
    private JButton buttonOkay;
    
    public OkayDialog(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        Dimension d = new Dimension(425, 500);
        setPreferredSize(d);
        setSize(d);
        
        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(this);
        buttonOkay = new JButton("Okay");
        buttonOkay.addActionListener(this);
        getRootPane().setDefaultButton(buttonOkay);
        
        JPanel okayPanel = layoutOkayCancelButtons(buttonOkay, buttonCancel);
        this.add(okayPanel, BorderLayout.PAGE_END);
    }
    
    public static JPanel layoutOkayCancelButtons(JButton okay, JButton cancel) {
        //Lay out the buttons from left to right.
        JPanel okayPanel = new JPanel();
        okayPanel.setLayout(new BoxLayout(okayPanel, BoxLayout.LINE_AXIS));
        okayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        okayPanel.add(Box.createHorizontalGlue());
        okayPanel.add(cancel);
        okayPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        okayPanel.add(okay);
        
        return okayPanel;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (buttonOkay == e.getSource()) {
            setVisible(false);
        } else if (buttonCancel == e.getSource()) {
            this.dispose();
        }
    }
    
    public JButton getButtonOkay() {
        return buttonOkay;
    }

    public JButton getButtonCancel() {
        return buttonCancel;
    }
}
