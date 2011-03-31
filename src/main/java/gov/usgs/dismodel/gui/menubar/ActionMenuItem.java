package gov.usgs.dismodel.gui.menubar;

import gov.usgs.dismodel.gui.components.AllGUIVars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

public abstract class ActionMenuItem extends JMenuItem {
    protected AllGUIVars allGuiVars;
    ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            menuItemClickAction(e);
        }
    };

    public ActionMenuItem(String title, AllGUIVars allGuiVars) {
        super(title);
        this.allGuiVars = allGuiVars;
        this.addActionListener(action);
    }

    public abstract void menuItemClickAction(ActionEvent e);

}
