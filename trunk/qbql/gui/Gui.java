package qbql.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import qbql.lattice.Database;
import qbql.parser.CYK;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.util.Util;

public class Gui {   

    private void frame( ParseNode root, List<LexerToken> src ) throws Exception {
        JFrame frame = new JFrame("Gui Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if( !root.contains(layout) )
            throw new Exception("Not a layout");
        
        layout(root, src, frame.getContentPane());
                        
        frame.pack();
        frame.setVisible(true);
    }
    
    private void layout( ParseNode root, List<LexerToken> src, Container contentPane ) {
        for( ParseNode child : root.children() ) {
            if( child.contains(nodes) )
                nodes(child, src, contentPane);
            else if( child.contains(digits) ) {
                int width = Integer.valueOf(child.content(src));
                GridLayout tblLayout = new GridLayout(0,width);
                contentPane.setLayout(tblLayout);
            }
        }
    }

    private void nodes( ParseNode root, List<LexerToken> src, Container contentPane ) {
        for( ParseNode child : root.children() ) {
            if( child.contains(layout) ) {
                JPanel p = new JPanel();
                contentPane.add(p);
                layout(child, src, p);
            } else if( child.contains(node) )
                node(child, src, contentPane);
            else if( child.contains(nodes) ) {
                nodes(child, src, contentPane);
            }
        }
    }

    private void node( ParseNode root, List<LexerToken> src, Container contentPane ) {
        if( root.contains(widget) ) 
            widget(root, src, contentPane);
    }

    private void widget( ParseNode root, List<LexerToken> src, Container contentPane ) {
        if( root.contains(button) ) 
            button(root, src, contentPane);
        else if( root.contains(input) ) 
            input(root, src, contentPane);
    }

    private void button( ParseNode root, List<LexerToken> src, Container contentPane ) {
        String label = null;
        JButton b = new JButton();
        for( ParseNode child : root.children() ) {
            if( child.contains(identifier) ) {
                label = child.content(src);
            }
        }
        b.setText(label);
        contentPane.add(b);
    }
    private void input( ParseNode root, List<LexerToken> src, Container contentPane ) {
        JLabel label = null;
        JTextField input = new JTextField(10);
        for( ParseNode child : root.children() ) {
            if( child.contains(identifier) ) {
                label = new JLabel(child.content(src)+":");
                label.setHorizontalTextPosition(JLabel.CENTER);
            }
        }
        JPanel p = new JPanel();
        p.add(label);
        p.add(input);
        //GridLayout tblLayout = new GridLayout(0,2);
        //p.setLayout(tblLayout);
        contentPane.add(p);
    }
    /////////////////////////GRAMMAR//////////////////////////
    private static Set<RuleTuple> guiRules() {
        Set<RuleTuple> ret = new TreeSet<RuleTuple>();
        ret.add(new RuleTuple("node", new String[] {"layout"}));
        ret.add(new RuleTuple("node", new String[] {"widget"}));
        ret.add(new RuleTuple("nodes", new String[] {"nodes","','","node"}));
        ret.add(new RuleTuple("nodes", new String[] {"node"}));
        ret.add(new RuleTuple("widget", new String[] {"button"}));
        ret.add(new RuleTuple("button", new String[] {"'button'", "identifier"}));
        ret.add(new RuleTuple("widget", new String[] {"input"}));
        ret.add(new RuleTuple("input", new String[] {"'input'", "identifier"}));
        ret.add(new RuleTuple("layout", new String[] {"'('","nodes","')'","'/'","digits"}));
        return ret;
    }
    
    static CYK cyk = new CYK(guiRules()) {
        public int[] atomicSymbols() {
            return new int[] {};
        }
    };
    static int layout;
    static int node;
    static int nodes;
    static int digits;
    static int widget;
    static int identifier;
    static int button;
    static int input;
    static {
        try {
            layout = cyk.symbolIndexes.get("layout");
            node = cyk.symbolIndexes.get("node");
            nodes = cyk.symbolIndexes.get("nodes");
            digits = cyk.symbolIndexes.get("digits");
            widget = cyk.symbolIndexes.get("widget");
            button = cyk.symbolIndexes.get("button");
            input = cyk.symbolIndexes.get("input");
            identifier = cyk.symbolIndexes.get("identifier");
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
        
    private static final String path = "/qbql/gui/";
    public static void main( String[] args ) throws Exception {
        Gui model = new Gui();
        String guiCode = Util.readFile(model.getClass(),path+"test.gui");

        List<LexerToken> src =  LexerToken.parse(guiCode);
        Matrix matrix = cyk.initArray1(src);
        int size = matrix.size();
        TreeMap<Integer,Integer> skipRanges = new TreeMap<Integer,Integer>();
        cyk.closure(matrix, 0, size+1, skipRanges, -1);
        ParseNode root = cyk.forest(size, matrix);

        if( root.topLevel != null ) {
            System.out.println("*** Parse Error in assertions file ***");
            CYK.printErrors(guiCode, src, root);
        }
        root.printTree();

        model.frame(root, src);
        
    }

}

