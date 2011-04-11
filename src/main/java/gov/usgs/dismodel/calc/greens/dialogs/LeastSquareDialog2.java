package gov.usgs.dismodel.calc.greens.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;

public class LeastSquareDialog2 extends LeastSquareDialogBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1759759826985233424L;
	private boolean applySubfaultBounds = false;
	private int rows;
	private int columns;
	private FaultPanel activeFault = null;
	private SlipBounds bounds;

	public enum Equals {
	    equality, lessThanOrEqual, greatherThanOrEqual;
	}
	
    /** Creates new form LeastSquaresDialog */
    public LeastSquareDialog2(int rows, int columns) {
        super();
        applyBoundsCBActionPerformed(null);
        setRowsAndColumns(rows, columns);
        bounds = new SlipBounds(rows, columns);
    }

	public static class SlipBounds {
	
	    public DistributedFault lower;
	    public DistributedFault upper;
	
	    public SlipBounds(int rows, int columns) {
	        lower = new DistributedFault(rows, columns);
	        upper = new DistributedFault(rows, columns);
	    }
	
	    public void setUB(int row, int col, double val) {
	        upper.getSubfaults()[row][col].setStrikeSlip(val);      //TODO:  Change this to refer to only 1
	        upper.getSubfaults()[row][col].setDipSlip(val);
	        upper.getSubfaults()[row][col].setOpening(val);
	    }
	    
	    
	    public void setLB(int row, int col, double val) {
	        lower.getSubfaults()[row][col].setStrikeSlip(val);      //TODO:  Change this to refer to only 1
	        lower.getSubfaults()[row][col].setDipSlip(val);
	        lower.getSubfaults()[row][col].setOpening(val);
	    }
	
	    public OkadaFault3 getUB(int row, int col) {
	        return upper.getSubfaults()[row][col];
	    }
	
	    public OkadaFault3 getLB(int row, int col) {
	        return lower.getSubfaults()[row][col];
	    }
	
	    // TODO getter in whatever form is most convenient
	}

	public static class FaultPanel extends JPanel {
		private static final long serialVersionUID = 1080832456473469540L;
		private JLabel label;
	    private int row;
	    private int col;
	
	    public FaultPanel(String name, int row, int col) {
	        super();
	        setName(name);
	        setMinimumSize(new Dimension(6, 6));
	        label = new JLabel(name);
	        add(label);
	        this.row = row;
	        this.col = col;
	    }
	
	    public void setColor(Color c) {
	        label.setForeground(c);
	    }
	
	    public int getRow() {
	        return row;
	    }
	    public int getCol() {
	        return col;
	    }
	}



	public DistributedFault getLB(){
	    return bounds.lower;
	}

	public DistributedFault getUB(){
	    return bounds.upper;
	}

	public boolean isApplySubfaultBounds() {
	    return applySubfaultBounds;
	}
	
    public final void setRowsAndColumns(int rows, int columns) {
        this.bounds = new SlipBounds(rows, columns);
        
        this.rows = rows;
        this.columns = columns;
        
        GridLayout gl = new GridLayout(this.rows, this.columns, 1, 1);
        gridPanel.removeAll();
        gridPanel.setLayout(gl);
        int count = 0;
        for(int i=0; i < rows; i++) {
            for (int j=0; j < columns; j++) {
                String label = Integer.toString(++count);
                FaultPanel p = new FaultPanel(label, i, j);
                
                p.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (activeFault != null)
                            activeFault.setColor(Color.LIGHT_GRAY);
                        activeFault = (FaultPanel) e.getComponent();
                        activeFault.setColor(Color.red);
                        int row = activeFault.getRow();
                        int col = activeFault.getCol();
                        double ub = bounds.getUB(row, col).getStrikeSlip();         //TODO:  Change this to a less hackish way
                        if (!Double.isNaN(ub)) {
                            upperBound.setText(Double.toString(ub));
                        } else {
                            upperBound.setText("");
                        }
                        Double lb = bounds.getLB(row, col).getStrikeSlip();         //TODO:  Change this to a less hackish way
                        if (!Double.isNaN(lb)) {
                            lowerBound.setText(Double.toString(lb));
                        } else {
                            lowerBound.setText("");
                        }
                    }
                });
                gridPanel.add(p);
            }
        }
        
        gridPanel.setBackground(Color.BLACK);
    }


	@Override
	protected void applyBoundsCBActionPerformed(java.awt.event.ActionEvent evt) {                                              
	    boolean selected = applyBoundsCB.isSelected();
	    lowerBound.setEnabled(selected);
	    upperBound.setEnabled(selected);
	    this.applySubfaultBounds = selected;
	}



	@Override
	protected void okayButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
	    this.setVisible(false);
	}

	@Override
	protected void lowerBoundActionPerformed(java.awt.event.ActionEvent evt) {                                           
	    if (activeFault == null)
	        return;
	
	    String text = lowerBound.getText();
	    if (text != null && !text.equals("")) {
	        try {
	            Double val = Double.parseDouble(text);
	            bounds.setLB(activeFault.getRow(), activeFault.getCol(), val);
	            System.out.println("SET LB TO:" + val + " for " + activeFault.getName());
	        } catch (Exception e) {
	            System.err.println(e);
	        }
	    }
	}

	@Override
	protected void upperBoundActionPerformed(java.awt.event.ActionEvent evt) {                                           
	   if (activeFault == null)
	        return;
	
	    String text = upperBound.getText();
	    if (text != null && !text.equals("")) {
	        try {
	            Double val = Double.parseDouble(text);
	            bounds.setUB(activeFault.getRow(), activeFault.getCol(), val);
	            System.out.println("SET UB TO:" + val + " for " + activeFault.getName());
	        } catch (Exception e) {
	            System.err.println(e);
	        }
	    }
	}


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	LeastSquareDialog2 dialog = new LeastSquareDialog2(10, 10);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }




}
