package gov.usgs.dismodel;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Displays a dialog box for loading a text file containing information about:<BR>
 * stations, station displacements or earthquakes.
 *  
 * @author dmcmanamon
 */
public class LoadStationsDialog extends OkayDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private JComboBox nameList;
    private JComboBox latList;
    private JComboBox lngList;
    private JComboBox heightList;
    
    private CountDownLatch doneSignal = new CountDownLatch(1);
    
    private List<Label> result = new ArrayList<Label>();
    
    private LineNumberReader file;
    private Pattern p;
    
    private JFrame frame;
    
    public LoadStationsDialog(JFrame owner, boolean modal, String[] columnNames,
            LineNumberReader file, Pattern p) {
        super(owner, "Load Stations Text File", modal);
        this.frame = owner;
        this.file = file;
        this.p = p;
        Dimension d = new Dimension(290, 185);
        setPreferredSize(d);
        setSize(d);

        JLabel nameLabel = new JLabel("Name:");
        nameList = new JComboBox(columnNames);
        nameList.setSelectedIndex(guessColumn(columnNames, "S", "N")); //Site, Station, Name
        nameList.addActionListener(this);
        
        JLabel latLabel = new JLabel("Latitude:");
        latList = new JComboBox(columnNames);
        latList.setSelectedIndex(guessColumn(columnNames, "La", "LA"));
        latList.addActionListener(this);
        
        JLabel lngLabel = new JLabel("Longitude:");
        lngList = new JComboBox(columnNames);
        lngList.setSelectedIndex(guessColumn(columnNames, "Lo", "Ln", "LO", "LN"));
        lngList.addActionListener(this);
        
        JLabel heightLabel = new JLabel("Height:");
        heightList = new JComboBox(columnNames);
        heightList.setSelectedIndex(guessColumn(columnNames, "H"));
        heightList.addActionListener(this);
        
        getButtonOkay().addActionListener(this);
        getButtonCancel().addActionListener(this);
        
        JPanel stationsGrid = new JPanel(new GridLayout(4,2));
        stationsGrid.add(nameLabel);
        stationsGrid.add(nameList);
        stationsGrid.add(latLabel);
        stationsGrid.add(latList);
        stationsGrid.add(lngLabel);
        stationsGrid.add(lngList);
        stationsGrid.add(heightLabel);
        stationsGrid.add(heightList);
        
        this.add(stationsGrid, BorderLayout.CENTER);
        
        pack();
    }
    
    private static int guessColumn(String [] columnNames, String... prefixes) {
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            for (String prefix : prefixes) {
                if (columnName.startsWith(prefix))
                    return i;
            }
            
        }
        return 0;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (getButtonOkay() == e.getSource()) {
                try {
                    String line = file.readLine();
                    while (line != null) {
                        List<String> data = SaveAndLoad.parse(line, p);
                        
                        double latitude = Double.parseDouble(data.get(latList.getSelectedIndex()));
                        double longitude = Double.parseDouble(data.get(lngList.getSelectedIndex()));
                        double height = Double.parseDouble(data.get(heightList.getSelectedIndex()));
                        String name = data.get(nameList.getSelectedIndex());
                        
                        LLH location = new LLH(latitude, longitude, height);
                        Label label = new Label(location, name);
                        
                        result.add(label);
                        line = file.readLine();
                    }
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, 
                            e1.getMessage(),
                            "Failed to load file", JOptionPane.ERROR_MESSAGE);
                    System.err.println(e1);
                    e1.printStackTrace();
                }
            dispose();
            doneSignal.countDown();
        } else if (getButtonCancel() == e.getSource()) {
            dispose();
            doneSignal.countDown();
        } 
    }

    public List<Label> getResult() {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return result;
    }
}