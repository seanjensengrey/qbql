package qbql.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import qbql.lattice.Relation;

public abstract class Util {
    public static void main(String[] args) throws Exception {
        /*Set<RuleTuple> rules = ParserEngine.extractRules();
		Set<String> keywords = ParserEngine.getKeywords(rules);
		for( String s : keywords )
			System.out.println("| "+s);  // (authorized)
         */
        int p = decrPair(pair(16,57));
        System.out.println("x= "+X(p)+", y= "+Y(p));  // (authorized)
    }

    public static String readFile( String file ) throws Exception {
        return readFile(new FileInputStream(file));
    }

    public static String readFile( Class c, String file ) throws Exception {
        URL u = c.getResource( file );
        return readFile(u.openStream());
    }

    public static String readFile(InputStream is) throws Exception {
        byte[] bytes = new byte[4096];
        int bytesRead = 0;
        BufferedInputStream bin = null;
        StringBuffer ret = new StringBuffer();
        try {
            bin = new BufferedInputStream(is);
            bytesRead = bin.read(bytes, 0, bytes.length);
            while (bytesRead != -1) {
                ret.append(new String(bytes).substring(0, bytesRead));
                bytesRead = bin.read(bytes, 0, bytes.length);
            }
        } finally {
            if (bin != null)
                bin.close();
        }
        return ret.toString();
    }

    public static String toNull( String src ) {
        return "".equals(src)? null : src;
    }

    public static String identln( int level, String txt ) {
        StringBuffer b = new StringBuffer();
        for(int i = 0; i< level;i++)
            b.append(" "); 
        b.append(txt); 
        return b.toString();
    }

    // interval indexes
    public static int pair( int x, int y ) {
        return (y<<16)|x;
    }
    public static int Y( int p ) {
        return p>>16;
    }
    public static int X( int p ) {
        return p&0xffff;
    }
    // x--; y--
    public static int decrPair( int p ) {
        return p-0x10001;
    }

}
