package gov.usgs.dismodel;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.sourcemodels.Quake;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
public class LoadQuakeDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JButton buttonOkay;
    private JButton buttonCancel;
    
    // for loading Earthquakes
    private JComboBox latListQuake;
    private JComboBox lngListQuake;
    private JComboBox heightListQuake;
    private JComboBox magnitudeQuake;
    
    private CountDownLatch doneSignal = new CountDownLatch(1);
    
    private List<Quake> result;
    
    private LineNumberReader file;
    private Pattern p;
    
    private JFrame frame;
    
    public LoadQuakeDialog(JFrame owner, boolean modal, String[] columnNames,
            LineNumberReader file, Pattern p) {
        super(owner, "Load Text File", modal);
        this.frame = owner;
        this.file = file;
        this.p = p;
        Dimension d = new Dimension(290, 185);
        setPreferredSize(d);
        setSize(d);
        setLayout(new BorderLayout());
        /*
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
        
        
        tabs.setBorder(BorderFactory.createLineBorder(Color.black));
        JPanel stationsGrid = new JPanel(new GridLayout(4,2));
        stationsGrid.add(nameLabel);
        stationsGrid.add(nameList);
        stationsGrid.add(latLabel);
        stationsGrid.add(latList);
        stationsGrid.add(lngLabel);
        stationsGrid.add(lngList);
        stationsGrid.add(heightLabel);
        stationsGrid.add(heightList);
        tabs.addTab("Stations", stationsGrid);
        */
        
        JLabel latLabelQuake = new JLabel("Latitude:");
        latListQuake = new JComboBox(columnNames);
        latListQuake.setSelectedIndex(guessColumn(columnNames, "La", "LA"));
        latListQuake.addActionListener(this);
        
        JLabel lngLabelQuake = new JLabel("Longitude:");
        lngListQuake = new JComboBox(columnNames);
        lngListQuake.setSelectedIndex(guessColumn(columnNames, "Lo", "Ln", "LO", "LN"));
        lngListQuake.addActionListener(this);
        
        JLabel heightLabelQuake = new JLabel("Height:");
        heightListQuake = new JComboBox(columnNames);
        heightListQuake.setSelectedIndex(guessColumn(columnNames, "H"));
        heightListQuake.addActionListener(this);
        
        JLabel magnitudeLabelQuake = new JLabel("Magnitude:");
        magnitudeQuake = new JComboBox(columnNames);
        magnitudeQuake.setSelectedIndex(guessColumn(columnNames, "M", "R"));
        magnitudeQuake.addActionListener(this);
        
        JPanel stationsGridQuake = new JPanel(new GridLayout(4,2));
        stationsGridQuake.add(latLabelQuake);
        stationsGridQuake.add(latListQuake);
        stationsGridQuake.add(lngLabelQuake);
        stationsGridQuake.add(lngListQuake);
        stationsGridQuake.add(heightLabelQuake);
        stationsGridQuake.add(heightListQuake);
        stationsGridQuake.add(magnitudeLabelQuake);
        stationsGridQuake.add(magnitudeQuake);
        
        this.add(stationsGridQuake);
        
        //Create and initialize the buttons.
        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(this);
        buttonOkay = new JButton("Okay");
        buttonOkay.addActionListener(this);
        getRootPane().setDefaultButton(buttonOkay);
        
        //Lay out the buttons from left to right.
        JPanel okayPanel = new JPanel();
        okayPanel.setLayout(new BoxLayout(okayPanel, BoxLayout.LINE_AXIS));
        okayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        okayPanel.add(Box.createHorizontalGlue());
        okayPanel.add(buttonCancel);
        okayPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        okayPanel.add(buttonOkay);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        
        contentPane.add(okayPanel, BorderLayout.PAGE_END);

        pack();
        setLocationRelativeTo(owner);
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
        if (buttonOkay == e.getSource()) {
                List<Quake> result = new ArrayList<Quake>();
                String line;
                try {
                    line = file.readLine();
                    while (line != null) {
                        List<String> data = SaveAndLoad.parse(line, p);
                        
                        double latitude = Double.parseDouble(data.get(latListQuake.getSelectedIndex()));
                        double longitude = Double.parseDouble(data.get(lngListQuake.getSelectedIndex()));
                        double height = Double.parseDouble(data.get(heightListQuake.getSelectedIndex()));
                        double magnitude = Double.parseDouble(data.get(magnitudeQuake.getSelectedIndex()));
                        
                        LLH location = new LLH(latitude, longitude, height);
                        Quake q = new Quake(null, location, magnitude);
                        
                        result.add(q);
                        line = file.readLine();
                    }
                    this.result = result;
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, 
                            e1.getMessage(),
                            "Failed to load file", JOptionPane.ERROR_MESSAGE);
                    System.err.println(e1);
                    e1.printStackTrace();
                }
            dispose();
            doneSignal.countDown();
        } else if (buttonCancel == e.getSource()) {
            dispose();
            doneSignal.countDown();
        } 
    }

    public List<Quake> getResult() {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return result;
    }
}