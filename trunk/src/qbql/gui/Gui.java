package qbql.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.parser.BNFGrammar;
import qbql.parser.CYK;
import qbql.parser.Lex;
import qbql.parser.LexerToken;
import qbql.parser.Matrix;
import qbql.parser.ParseNode;
import qbql.parser.RuleTuple;
import qbql.util.Util;

public class Gui {   

    private void frame( ParseNode root, List<LexerToken> src ) throws Exception {
        JFrame frame = new JFrame("Gui Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if( !root.contains(grid) )
            throw new Exception("Not a grid!");
        
        grid(root, src, frame.getContentPane());
                        
        frame.pack();
        frame.setVisible(true);
    }
    
    private void grid( ParseNode root, List<LexerToken> src, Container contentPane ) {
        for( ParseNode child : root.children() ) {
            if( child.contains(nodes) )
                nodes(child, src, contentPane);
            else if( child.contains(digits) ) {
                int width = Integer.valueOf(child.content(src));
                //GroupLayout layout = new GroupLayout(contentPane);
                GridLayout layout = new GridLayout(0,width);
                contentPane.setLayout(layout);
                //layout.setAutoCreateGaps(true);
                //layout.setAutoCreateContainerGaps(true);
            }
        }
    }

    private void nodes( ParseNode root, List<LexerToken> src, Container contentPane ) {
        for( ParseNode child : root.children() ) {
            if( child.contains(grid) ) {
                JPanel p = new JPanel();
                contentPane.add(p);
                grid(child, src, p);
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
        else if( root.contains(codeArea) ) 
            codeArea(root, src, contentPane);
        else if( root.contains(padding) ) 
            padding(root, src, contentPane);
    }

    private void button( ParseNode root, List<LexerToken> src, Container contentPane ) {
        String label = null;
        JButton b = new JButton();
        for( ParseNode child : root.children() ) {
            if( child.contains(identifier) ) {
                label = child.content(src);
            }
            if( child.contains(string_literal) ) {
                label = child.content(src).substring(1,child.content(src).length()-1);
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
        GridLayout tblLayout = new GridLayout(0,2);
        p.setLayout(tblLayout);
        contentPane.add(p);
    }
    private void codeArea( ParseNode root, List<LexerToken> src, Container contentPane ) {
        JScrollPane editorScrollPane = new JScrollPane(new JEditorPane());
        editorScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
        contentPane.add(editorScrollPane);
    }
    private void padding( ParseNode root, List<LexerToken> src, Container contentPane ) {
        int width = 10;
        for( ParseNode child : root.children() ) {
            if( child.contains(digits) ) {
                width = Integer.valueOf(child.content(src));
            }
        }
        contentPane.add(Box.createRigidArea(new Dimension(width,width)));
    }
    /////////////////////////GRAMMAR//////////////////////////
    private static Set<RuleTuple> guiRules() throws Exception {
        String input = Util.readFile(Gui.class, "gui.grammar");
        HashMap<String, String> specialSymbols = new HashMap<String, String>();
        List<LexerToken> src = new Lex(
                      true, true, false,
                      specialSymbols                 
        ).parse(input);
        //LexerToken.print(src);
        ParseNode root = BNFGrammar.parseGrammarFile(src, input);
        return BNFGrammar.grammar(root, src);
    }
    
    static CYK cyk = null; 
    static int grid;
    static int node;
    static int nodes;
    static int digits;
    static int widget;
    static int identifier;
    static int string_literal;
    static int button;
    static int input;
    static int codeArea;
    static int padding;
    static {
        try {
            cyk = new CYK(guiRules()) {
                public int[] atomicSymbols() {
                    return new int[] {};
                }
            };            grid = cyk.symbolIndexes.get("grid");
            node = cyk.symbolIndexes.get("node");
            nodes = cyk.symbolIndexes.get("nodes");
            digits = cyk.symbolIndexes.get("digits");
            widget = cyk.symbolIndexes.get("widget");
            button = cyk.symbolIndexes.get("button");
            input = cyk.symbolIndexes.get("input");
            codeArea = cyk.symbolIndexes.get("codeArea");
            padding = cyk.symbolIndexes.get("padding");
            identifier = cyk.symbolIndexes.get("identifier");
            string_literal = cyk.symbolIndexes.get("string_literal");
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
        
    private static final String path = "/qbql/gui/";
    public static void main( String[] args ) throws Exception {
        Gui model = new Gui();
        String guiCode = Util.readFile(model.getClass(),path+"test.gui");

        List<LexerToken> src =  new Lex().parse(guiCode);
        //LexerToken.print(src);
        Matrix matrix = cyk.initMatrixSubdiagonal(src);
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

