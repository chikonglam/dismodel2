package gov.usgs.dismodel.gui.components;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MainMenu extends JMenuBar {

	public MainMenu() {
		//Main Menu
		super();
		this.setName("menuBar");
		
		//File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("fileMenu");
        this.add(fileMenu);
        
        //Map Menu
        JMenu mapMenu = new JMenu("Map");
        mapMenu.setName("mapMenu");
        this.add(mapMenu);
        
        //Data Menu
        JMenu dataMenu = new JMenu("Data");
        dataMenu.setName("dataMenu");
        this.add(dataMenu);
        
        //Source Menu
        JMenu sourceMenu = new JMenu("Source");
        sourceMenu.setName("sourceMenu");
        this.add(sourceMenu);
        
        //Inversion Menu
        JMenu inversionMenu = new JMenu("Inversion");
        inversionMenu.setName("inversionMenu");
        this.add(inversionMenu);
        
        //Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setName("helpMenu");
        this.add(helpMenu);
	}
	

}
