package qbql.parser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.text.html.HTMLEditorKit;

import qbql.util.Util;

public class Visual {
    public static boolean[][] skipped = null;
    List<LexerToken> src;
    CYK cyk;
    private int zoom = 1;
    private int offset = 0;
    public Visual( final List<LexerToken> s, CYK c ) {
        src = s;
        cyk = c;
        skipped = new boolean[src.size()+1][src.size()+1];
        zoom = 1+400/src.size();
        offset = zoom/2;
        if( zoom == 1 )
            offset = 0;
    }
    public void draw( final Matrix matrix ) {		
        final int size = src.size();
        final int X = (size+1)*zoom, Y = (size+1)*zoom;
        byte[] pixels = new byte[X * Y]; 
        for( int j = 0; j < Y; ++j)
            for( int i = 0; i < X; ++i) {
                int z = (j * X + i);
                int[] tmp = matrix.get(Util.pair(i/zoom, j/zoom));
                if( tmp!=null ) {
                    pixels[z] = 0;
                } else {
                    if( skipped[i/zoom][j/zoom] || j <= i ) {
                        pixels[z] = 1;
                    } else {
                        pixels[z] = 4;
                    }
                }
            }
//      Create a data buffer using the byte buffer of pixel data.
        // The pixel data is not copied; the data buffer uses the byte buffer array.
        DataBuffer dbuf = new DataBufferByte(pixels, X*Y, 0);
        int numBanks = dbuf.getNumBanks(); // 1
        int bitMasks[] = new int[]{(byte)0xf};
        SampleModel sampleModel = new SinglePixelPackedSampleModel(
                                                                   DataBuffer.TYPE_BYTE, X, Y, bitMasks);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);
        ColorModel colorModel = generateColorModel();
        final BufferedImage img = new BufferedImage(colorModel, raster, false, null);//new java.util.Hashtable());		

        final JFrame frame = new JFrame("CYK Matrix"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
        final JEditorPane t = new JEditorPane();
        t.setEditorKit(new HTMLEditorKit());
        JScrollPane editorScrollPane = new JScrollPane(t);
        editorScrollPane.setVerticalScrollBarPolicy(
                                                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(10, 300));
        editorScrollPane.setMinimumSize(new Dimension(10, 50));

        class ScrollablePicture extends JLabel // Canvas
        implements Scrollable, MouseMotionListener, MouseWheelListener {

            public ScrollablePicture() {
                super(new Icon() {
                    public int getIconHeight() {
                        return Y;
                    }
                    public int getIconWidth() {
                        return X;
                    }
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        g.drawImage(img, 0, 0, Color.orange, null);
                        drawSrcText(g);
                    }

                });
                setAutoscrolls(true); //enable synthetic drag events
                addMouseMotionListener(this); //handle mouse drags
                addMouseWheelListener(this);  //switch symbols
            }
            public void mouseDragged( MouseEvent e ) {
                Rectangle r = new Rectangle(e.getX(), e.getY(), 10, 10);
                scrollRectToVisible(r);
            }
            public void mouseMoved( MouseEvent e ) {
                int x = e.getX()/zoom;
                int y = e.getY()/zoom;
                e.consume();
                output = matrix.get(Util.pair(x, y));
                if( output !=  null ) { 
                    Map<Integer,Integer> symbols = new HashMap<Integer,Integer>();
                    for( int kk : output ) {
                        int k = Util.Y(kk);
                        Integer val = symbols.get(k);
                        if( val == null )
                            symbols.put(k,1);
                        else    
                            symbols.put(k,val+1);
                    }                    
                    StringBuffer sb = new StringBuffer("<html><font color=red>["+x+","+y+")</font><br>");   //$NON-NLS-2$ //$NON-NLS-3$
                    for( int k : symbols.keySet() ) {
                        if( k == -1 )
                            sb.append("<font color=red>-1</font>"); 
                        else {
                            String symbol = cyk.allSymbols[k];
                            if( symbol.indexOf('[') < 0 && symbol.indexOf('+') < 0 && symbol.indexOf('.')<0 )
                                symbol = "<b>"+symbol+"</b>";  
                            sb.append("  "+symbol+(symbols.get(k)<5?"":(" <font color=pink size=\""+(symbols.get(k)-7)+"\">"+symbols.get(k)+"</font>"))); // (authorized)  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                        }
                    }
                    sb.append("<font color=green><br><br>"); 
                    for( int i = x; i < y; i++ ) {
                        sb.append(" "+src.get(i).content); 
                    }
                    t.setText(sb.toString());
                    //print(matrix, x, y);
                    x0 = x;
                    y0 = y;
                    //repaint(0, x, mid, mid-x, y-mid);
                    repaint();
                } else if( x0 != -1 ) {
                    repaint();
                    //repaint(0, x0, mid, mid-x0, y0-mid);
                    x0 = -1;
                    y0 = -1;
                    output = null;
                    index = 0;
                }
            }
            int x0 = -1;
            int y0 = -1;
            int[] output = null;
            int index = 0;
            public Dimension getPreferredScrollableViewportSize() {
                // TODO Auto-generated method stub
                return null;
            }
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                // TODO Auto-generated method stub
                return 0;
            }
            public boolean getScrollableTracksViewportHeight() {
                // TODO Auto-generated method stub
                return false;
            }
            public boolean getScrollableTracksViewportWidth() {
                // TODO Auto-generated method stub
                return false;
            }
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                // TODO Auto-generated method stub
                return 0;
            }
            public void paint(Graphics g) {
                super.paint(g);
                if( x0 != -1 ) {
                    g.setColor(Color.red);
                    if( index >= output.length )
                        index = 0;
                    int mid = Util.X(output[index]);
                    g.drawLine(x0*zoom+offset, mid*zoom+offset, x0*zoom+offset, y0*zoom+offset);
                    g.drawLine(x0*zoom+offset, y0*zoom+offset, mid*zoom+offset, y0*zoom+offset);
                }
            }
            
            public void mouseWheelMoved(MouseWheelEvent e) {
                index++;
                repaint();
            }
        }
        JScrollPane canvasScrollPane = new JScrollPane(new ScrollablePicture());
        canvasScrollPane.setVerticalScrollBarPolicy(
                                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        canvasScrollPane.setHorizontalScrollBarPolicy(
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        canvasScrollPane.setPreferredSize(new Dimension(900, 900));
        canvasScrollPane.setMinimumSize(new Dimension(50, 50));


        frame.getContentPane().add(canvasScrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(editorScrollPane, BorderLayout.SOUTH);
        frame.pack();
        //frame.setSize(X, Y+350);
        frame.setVisible(true);

    }

    private void drawSrcText( Graphics g ) {
        if( zoom < 5 )
            return;
        int pos = -1;
        for( LexerToken t : src ) {
            pos++;
            //g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, ));
            g.setColor(Color.WHITE);
            g.drawString(t.content, (pos)*zoom + 2*zoom/3, (pos+1)*zoom + 2*zoom/3);
        }
    }
    
    private static ColorModel generateColorModel() {
        // Generate 16-color model
        byte[] r = new byte[16];
        byte[] g = new byte[16];
        byte[] b = new byte[16];

        r[0] = 0; g[0] = 0; b[0] = 0;
        r[1] = 0; g[1] = 0; b[1] = (byte)192;
        r[2] = 0; g[2] = 0; b[2] = (byte)255;
        r[3] = 0; g[3] = (byte)192; b[3] = 0;
        r[4] = 0; g[4] = (byte)255; b[4] = 0;
        r[5] = 0; g[5] = (byte)192; b[5] = (byte)192;
        r[6] = 0; g[6] = (byte)255; b[6] = (byte)255;
        r[7] = (byte)192; g[7] = 0; b[7] = 0;
        r[8] = (byte)255; g[8] = 0; b[8] = 0;
        r[9] = (byte)192; g[9] = 0; b[9] = (byte)192;
        r[10] = (byte)255; g[10] = 0; b[10] = (byte)255;
        r[11] = (byte)192; g[11] = (byte)192; b[11] = 0;
        r[12] = (byte)255; g[12] = (byte)255; b[12] = 0;
        r[13] = (byte)80; g[13] = (byte)80; b[13] = (byte)80;
        r[14] = (byte)192; g[14] = (byte)192; b[14] = (byte)192;
        r[15] = (byte)255; g[15] = (byte)255; b[15] = (byte)255;

        return new IndexColorModel(4, 16, r, g, b);
    }	

}
