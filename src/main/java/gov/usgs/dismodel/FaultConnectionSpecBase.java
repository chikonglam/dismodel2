/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FaultConnectionSpecBase.java
 *
 * Created on Feb 24, 2011, 5:41:10 PM
 */

package gov.usgs.dismodel;

/**
 *
 * @author clam-PR
 */
public class FaultConnectionSpecBase extends javax.swing.JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7530432212320555602L;
	/** Creates new form FaultConnectionSpecBase */
    public FaultConnectionSpecBase(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        baseScrollPane = new javax.swing.JScrollPane();
        basePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabUnconned = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtNoSegments = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabGroupSelected = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        cbGroup = new javax.swing.JComboBox();
        btnOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Specify connected subfaults");

        baseScrollPane.setAutoscrolls(true);

        tabUnconned.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null}
            },
            new String [] {
                "Details"
            }
        ) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 8131437173597731498L;
			Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabUnconned.setColumnSelectionAllowed(true);
        tabUnconned.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tabUnconned);
        tabUnconned.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabUnconned.getColumnModel().getColumn(0).setResizable(false);

        jLabel1.setText("Number of groups of connected segments:");

        txtNoSegments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNoSegmentsActionPerformed(evt);
            }
        });
        txtNoSegments.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNoSegmentsKeyReleased(evt);
            }
        });

        jLabel2.setText("(Note:  Changing number of group will reset grouping)");

        btnAdd.setText(">");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("<");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        jLabel3.setText("Unconnected segments:");

        tabGroupSelected.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null}
            },
            new String [] {
                "Details"
            }
        ) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1182273265851284442L;
			Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabGroupSelected.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(tabGroupSelected);
        tabGroupSelected.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabGroupSelected.getColumnModel().getColumn(0).setResizable(false);

        jLabel4.setText("Connected group");

        cbGroup.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGroupActionPerformed(evt);
            }
        });

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout basePanelLayout = new javax.swing.GroupLayout(basePanel);
        basePanel.setLayout(basePanelLayout);
        basePanelLayout.setHorizontalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelLayout.createSequentialGroup()
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNoSegments, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRemove)
                            .addComponent(btnAdd)))
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addGap(18, 18, 18)
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basePanelLayout.createSequentialGroup()
                .addContainerGap(724, Short.MAX_VALUE)
                .addComponent(btnOK)
                .addContainerGap())
        );
        basePanelLayout.setVerticalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelLayout.createSequentialGroup()
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtNoSegments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3))
                    .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(cbGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(btnAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemove))
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(btnOK)
                .addContainerGap())
        );

        baseScrollPane.setViewportView(basePanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(baseScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(baseScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    private void txtNoSegmentsActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        onBtnAddActionPerformed(evt);
    }

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        onBtnRemoveActionPerformed(evt);
    }

    private void cbGroupActionPerformed(java.awt.event.ActionEvent evt) {
        onCbGroupActionPerformed(evt);
    }

    private void txtNoSegmentsKeyReleased(java.awt.event.KeyEvent evt) {
        onTxtNoSegmentsKeyReleased(evt);
    }

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
        onBtnOKActionPerformed(evt);
    }

    //**Methods that should be overloaded
    protected void onTxtNoSegmentsKeyReleased(java.awt.event.KeyEvent evt) {}
    
    protected void onBtnAddActionPerformed(java.awt.event.ActionEvent evt) {}

    protected void onBtnRemoveActionPerformed(java.awt.event.ActionEvent evt) {}
    
    protected void onCbGroupActionPerformed(java.awt.event.ActionEvent evt) {}

    protected void onBtnOKActionPerformed(java.awt.event.ActionEvent evt) {}
    //**end of methods that should be overloaded

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FaultConnectionSpecBase dialog = new FaultConnectionSpecBase(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    protected javax.swing.JPanel UnconnectedPane1;
    protected javax.swing.JPanel basePanel;
    protected javax.swing.JScrollPane baseScrollPane;
    protected javax.swing.JButton btnAdd;
    protected javax.swing.JButton btnOK;
    protected javax.swing.JButton btnRemove;
    protected javax.swing.JComboBox cbGroup;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel4;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JScrollPane jScrollPane3;
    protected javax.swing.JTable tabUnconned;
    protected javax.swing.JTable tabUnconned1;
    protected javax.swing.JTable tabGroupSelected;
    protected javax.swing.JTextField txtNoSegments;
    // End of variables declaration

}
