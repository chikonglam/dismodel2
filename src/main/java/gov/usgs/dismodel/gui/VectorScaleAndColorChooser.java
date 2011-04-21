package gov.usgs.dismodel.gui;

import gov.usgs.dismodel.OkayDialog;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel for setting the color and scale of displacement vectors.
 * <BR>
 * Typical displacements of GPS stations are a few centimeters/year which is too small
 * to see on a map covering several kilometers.
 * 
 * @author dmcmanamon
 */
public class VectorScaleAndColorChooser extends JDialog {
    private static final long serialVersionUID = 6164706528927273798L;

    private static final String SLIDER_TITLE = "Displacement Vector Multiplier";
    
    private JButton buttonCancel;
    private JButton buttonOkay;
    
    private final JColorChooser chooser;
    private JSlider slider;
    private JLabel sliderLabel;
    
    public VectorScaleAndColorChooser(JFrame parent, DisplayStateStore displaySettings, Color initialColor) {
        super(parent, "Set Scale and Choose a Color", true);
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        int initialValue = displaySettings.getDisplacementVectorScale();
        
        sliderLabel = new JLabel(sliderTitleAndScale(initialValue), JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ChangeListener sliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String title = sliderTitleAndScale(getScale());
                sliderLabel.setText(title);
            }
        };
        slider = new JSlider(JSlider.HORIZONTAL, 0, 100000, initialValue);
        slider.setMajorTickSpacing(10000);
        slider.setMinorTickSpacing(1000);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(sliderListener);
        
        
        mainPanel.add(sliderLabel);
        mainPanel.add(slider);
        
        chooser = new JColorChooser(initialColor);
        chooser.setDragEnabled(true);
        mainPanel.add(chooser);
        
        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        };
        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(cancelListener);
        buttonOkay = new JButton("Okay");
        getRootPane().setDefaultButton(buttonOkay);
        
        JPanel okayPanel = OkayDialog.layoutOkayCancelButtons(buttonOkay, buttonCancel);
        mainPanel.add(okayPanel);
        
        this.setContentPane(mainPanel);
    }
    
    public String sliderTitleAndScale(int scale) {
        return SLIDER_TITLE + "   x" + Integer.toString(scale);
    }
    public Color getColor() {
        return chooser.getColor();
    }
    
    public int getScale() {
        if (slider.getValue() <= 0)
            return 1;
        return slider.getValue();
    }

    public JButton getButtonCancel() {
        return buttonCancel;
    }

    public JButton getButtonOkay() {
        return buttonOkay;
    }

    public JSlider getSlider() {
        return slider;
    }
}
