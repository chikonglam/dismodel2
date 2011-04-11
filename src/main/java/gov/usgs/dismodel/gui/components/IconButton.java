package gov.usgs.dismodel.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.usgs.dismodel.Dismodel2;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public abstract class IconButton extends JButton{
    private ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonClicked();
        }
    };
    
    
    public IconButton(String toolTip, String IconLocation){
        super( new ImageIcon( Dismodel2.class.getResource(IconLocation) ) );
        this.setToolTipText(toolTip);
        this.addActionListener(action);
    }
    
    protected abstract void buttonClicked();
}
