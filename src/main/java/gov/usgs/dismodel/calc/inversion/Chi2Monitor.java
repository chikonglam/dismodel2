/* Adapted from: @(#)MemoryMonitor.java	1.38 10/01/12
 * 
 * (The original MemoryMonitor.java is) 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */


package gov.usgs.dismodel.calc.inversion;

import static java.awt.Color.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/**
 * Scrolling, real-time graph to display progress of long calculations
 */
public class Chi2Monitor extends JPanel {

    private static final long serialVersionUID = -2078647239626583133L;
    static JCheckBox dateStampCB = new JCheckBox("Output Date Stamp");
    public Surface surf;
    JPanel controls;
    boolean doControls;
    JTextField tf;

    public Chi2Monitor() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(new EtchedBorder(), "Inversion Chi^2"));
        add(surf = new Surface());
        controls = new JPanel();
        controls.setPreferredSize(new Dimension(135,80));
        Font font = new Font("serif", Font.PLAIN, 10);
        JLabel label = new JLabel("Sample Rate");
        label.setFont(font);
        label.setForeground(BLACK);
        controls.add(label);
        tf = new JTextField("1000");
        tf.setPreferredSize(new Dimension(45,20));
        controls.add(tf);
        controls.add(label = new JLabel("ms"));
        label.setFont(font);
        label.setForeground(BLACK);
        controls.add(dateStampCB);
        dateStampCB.setFont(font);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               removeAll();
               if ((doControls = !doControls)) {
                   surf.stop();
                   add(controls);
               } else {
                   try { 
                       surf.sleepAmount = Long.parseLong(tf.getText().trim());
                   } catch (Exception ex) {}
                   surf.start();
                   add(surf);
               }
               revalidate();
               repaint();
            }
        });
    }
    
    public float getCurChi2() {
        return surf.getCurChi2();
    }

    public float getMaxChi2() {
        return surf.getMaxChi2();
    }

    public void setCurChi2(float curChi2) {
        surf.setCurChi2(curChi2);
    }

    public void setMaxChi2(float maxChi2) {
        surf.setMaxChi2(maxChi2);
    }



    public class Surface extends JPanel implements Runnable {

        public Thread thread;
        public long sleepAmount = 100;
        private int w, h;
        private BufferedImage bimg;
        private Graphics2D big;
        private Font font = new Font("Times New Roman", Font.PLAIN, 11);
        //private Runtime r = Runtime.getRuntime();
        private int columnInc;
        private int pts[];
        private int ptNum;
        private int ascent, descent;
        private float curChi2, maxChi2;
        private Rectangle graphOutlineRect = new Rectangle();
        private Rectangle2D mfRect = new Rectangle2D.Float();
        private Rectangle2D muRect = new Rectangle2D.Float();
        private Line2D graphLine = new Line2D.Float();
        private Color graphColor = new Color(46, 139, 87);
        private Color mfColor = new Color(0, 100, 0);
        private String usedStr;
      

        public Surface() {
            setBackground(BLACK);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int buttonClicked = e.getButton();
                    if (buttonClicked == MouseEvent.BUTTON1){
                        maxChi2 /= 2;
                    } else if (buttonClicked == MouseEvent.BUTTON3){
                        maxChi2 *= 2;
                    }
                }
            });
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            return new Dimension(135,80);
        }
        
        

            
        public float getCurChi2() {
            return curChi2;
        }

        public float getMaxChi2() {
            return maxChi2;
        }

        public void setCurChi2(float curChi2) {
            this.curChi2 = curChi2;
        }

        public void setMaxChi2(float maxChi2) {
            this.maxChi2 = maxChi2;
        }

        public void paint(Graphics g) {

            if (big == null) {
                return;
            }

            big.setBackground(getBackground());
            big.clearRect(0,0,w,h);

            //float freeMemory = (float) r.freeMemory();
            //float totalMemory = (float) r.totalMemory();

            // .. Draw allocated and used strings ..
            big.setColor(GREEN);
            big.drawString(String.format("Scale max:%.5e", maxChi2),  4.0f, (float) ascent+0.5f);
            usedStr = String.format("Current:%.5e", curChi2);
            big.drawString(usedStr, 4, h-descent);

            // Calculate remaining size
            float ssH = ascent + descent;
            float remainingHeight = (float) (h - (ssH*2) - 0.5f);
            //float blockHeight = remainingHeight/10;
            float blockWidth = 20.0f;
            float remainingWidth = (float) (w - blockWidth - 10);
            float diffChi2Range = maxChi2 - curChi2;

            // .. Memory Free ..
//            big.setColor(mfColor);
//            int MemUsage = (int) ((diffChi2Range / maxChi2) * 10);
//            int i = 0;
//            for ( ; i < MemUsage ; i++) { 
//                mfRect.setRect(5,(float) ssH+i*blockHeight,
//                                blockWidth,(float) blockHeight-1);
//                big.fill(mfRect);
//            }

            // .. Memory Used ..
//            big.setColor(GREEN);
//            for ( ; i < 10; i++)  {
//                muRect.setRect(5,(float) ssH+i*blockHeight,
//                                blockWidth,(float) blockHeight-1);
//                big.fill(muRect);
//            }

            // .. Draw History Graph ..
            big.setColor(graphColor);
            int graphX = 30;
            int graphY = (int) ssH;
            int graphW = w - graphX - 5;
            int graphH = (int) remainingHeight;
            graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
            big.draw(graphOutlineRect);

            int graphRow = graphH/10;

            // .. Draw row ..
            for (int j = graphY; j <= graphH+graphY; j += graphRow) {
                graphLine.setLine(graphX,j,graphX+graphW,j);
                big.draw(graphLine);
            }
        
            // .. Draw animated column movement ..
            int graphColumn = graphW/15;

            if (columnInc == 0) {
                columnInc = graphColumn;
            }

            for (int j = graphX+columnInc; j < graphW+graphX; j+=graphColumn) {
                graphLine.setLine(j,graphY,j,graphY+graphH);
                big.draw(graphLine);
            }

            --columnInc;

            if (pts == null) {
                pts = new int[graphW];
                ptNum = 0;
            } else if (pts.length != graphW) {
                int tmp[] = null;
                if (ptNum < graphW) {     
                    tmp = new int[ptNum];
                    System.arraycopy(pts, 0, tmp, 0, tmp.length);
                } else {        
                    tmp = new int[graphW];
                    System.arraycopy(pts, pts.length-tmp.length, tmp, 0, tmp.length);
                    ptNum = tmp.length - 2;
                }
                pts = new int[graphW];
                System.arraycopy(tmp, 0, pts, 0, tmp.length);
            } else {
                big.setColor(YELLOW);
                pts[ptNum] = (int)(graphY+graphH*(diffChi2Range/maxChi2));
                for (int j=graphX+graphW-ptNum, k=0;k < ptNum; k++, j++) {
                    if (k != 0) {
                        if (pts[k] != pts[k-1]) {
                            big.drawLine(j-1, pts[k-1], j, pts[k]);
                        } else {
                            big.fillRect(j, pts[k], 1, 1);
                        }
                    }
                }
                if (ptNum+2 == pts.length) {
                    // throw out oldest point
                    for (int j = 1;j < ptNum; j++) {
                        pts[j-1] = pts[j];
                    }
                    --ptNum;
                } else {
                    ptNum++;
                }
            }
            g.drawImage(bimg, 0, 0, this);
        }


        public void start() {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("MemoryMonitor");
            thread.start();
        }


        public synchronized void stop() {
            thread = null;
            notify();
        }


        public void run() {

            Thread me = Thread.currentThread();

            while (thread == me && !isShowing() || getSize().width == 0) {
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) { return; }
            }
    
            while (thread == me && isShowing()) {
                Dimension d = getSize();
                if (d.width != w || d.height != h) {
                    w = d.width;
                    h = d.height;
                    bimg = (BufferedImage) createImage(w, h);
                    big = bimg.createGraphics();
                    big.setFont(font);
                    FontMetrics fm = big.getFontMetrics(font);
                    ascent = (int) fm.getAscent();
                    descent = (int) fm.getDescent();
                }
                repaint();
                try {
                    thread.sleep(sleepAmount);
                } catch (InterruptedException e) { break; }
                if (Chi2Monitor.dateStampCB.isSelected()) {
                     System.out.println(new Date().toString() + " " + usedStr);
                }
            }
            thread = null;
        }
    }


    public static void main(String s[]) {
        final Chi2Monitor demo = new Chi2Monitor();
        demo.setCurChi2(50000000);
        demo.setMaxChi2(200000000);
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { demo.surf.start(); }
            public void windowIconified(WindowEvent e) { demo.surf.stop(); }
        };
        JFrame f = new JFrame("Chi^2 Monitor");
        f.addWindowListener(l);
        f.getContentPane().add("Center", demo);
        f.pack();
        f.setSize(new Dimension(400,400));
        f.setVisible(true);
        demo.surf.start();
    }
    
    public void rename(String name) {
        setBorder(new TitledBorder(new EtchedBorder(), name));
    }

}
