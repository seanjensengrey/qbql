package qbql.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import qbql.util.Util;

public class LexerToken {
	public String content;
	public int begin;
	public int end;
	public Token type;

	public LexerToken( CharSequence text, int from, int to, Token t ) {
		content = text.toString();
		begin = from;
		end = to;
		type = t;
	}
	
	public void print() {
		System.out.println(toString()); // (authorized)
	}
	public String toString() {
		return "["+begin+","+end+") "+content+"   <"+type+">"; 
	}
	public static void print( List<LexerToken> src ) {
		int j = 0;
		for( LexerToken t: src ) {
			System.out.print(j+"    "); // (authorized)
			t.print(); 
			j++;
		}
	}
	public static void print( List<LexerToken> src, int from, int to ) {		
		for( int i = from; i < to; i++ ) {
			System.out.print(" "+src.get(i).content); // (authorized)
		}
		System.out.println(); // (authorized)
	}
    // homegrown one pass method -- should be faster than regexpr based one
	public static boolean isPercentLineComment = false; // Prover9 comments
	public static boolean isQuotedString = false; // Prover9 unary '
    public static LinkedList<LexerToken> tokenize( String sourceExpr ) {
    	
    	LinkedList<LexerToken> ret = new LinkedList<LexerToken>();
    	final String operation = "()^-|!*+./><='\",;:%@";
    	final String ws = " \n\r\t";
        StringTokenizer st = new StringTokenizer(sourceExpr,
        		//".*-+/|><=()\'\", \n\r\t"
        		  operation + ws
        ,true);
		long t2 = System.currentTimeMillis();
        StringBuffer[] quotedLiteral = new StringBuffer[2];
        int pos = 0;
        while( st.hasMoreTokens() ) {
            String token = st.nextToken();
            pos += token.length();
            // comments
            LexerToken last = null;
            if( ret.size() > 0 )
            	last = ret.get(ret.size()-1);
            if( last != null && last.type == Token.COMMENT && (!last.content.endsWith("*/") || last.content.equals("/*/")) ) {
            	if( "*".equals(token) || "/".equals(token) )
            		last.content = last.content + token;
            	else
            		last.content = "/* ... "; // Speeds up really long comments.
            	                              // If needed the comment content
            	                              // then switch last.content to StringBuffer
            	last.end = last.begin + last.content.length();
                continue;
            }
            if( last != null && last.type == Token.COMMENT && last.content.endsWith("*/") && !last.content.equals("/*/") ) {
            	last.end = pos-token.length();
            }
            if( last != null && last.type == Token.LINE_COMMENT && !"\n".equals(token) ) {
            	last.content = last.content + token;
                continue;
            }
            if( last != null && last.type == Token.LINE_COMMENT && "\n".equals(token) ) {
            	last.end = pos-token.length();
            	last.end = last.begin + last.content.length();
            }
            if( isQuotedString && last != null && last.type == Token.QUOTED_STRING && !"'".equals(token)
            		&& !(last.content.endsWith("'")&&last.content.length()>1)) {
            	last.content = last.content + token;
            	last.end = last.begin + last.content.length();
                continue;
            }
            if( isQuotedString && last != null && last.type == Token.QUOTED_STRING && "'".equals(token) 
            		&& !(last.content.endsWith("'")&&last.content.length()>1)) {
            	last.content = last.content + token;
            	last.end = pos;
                continue;
            }
            if( last != null && last.type == Token.DQUOTED_STRING && !"\"".equals(token) 
            		&& !(last.content.endsWith("\"")&&last.content.length()>1)) {
            	last.content = last.content + token;
            	last.end = last.begin + last.content.length();
                continue;
            }
            if( last != null && last.type == Token.DQUOTED_STRING && "\"".equals(token) ) {
            	last.content = last.content + token;
            	last.end = pos;
            	continue;
            }
            
            if( "*".equals(token) && last != null && "/".equals(last.content) ) {
            	last.content = last.content + token;
            	last.end = last.begin + last.content.length();
            	last.type = Token.COMMENT;
                continue;
            }
            if( "-".equals(token) && last != null && "-".equals(last.content) ) {
            	last.content = last.content + token;
            	last.type = Token.LINE_COMMENT;
                continue;
            }
            if( isPercentLineComment && "%".equals(token) ) {
            	ret.add(new LexerToken(token, pos-1, -10, Token.LINE_COMMENT));
                continue;
            }
            if( isQuotedString && "'".equals(token) ) {  // start
            	ret.add(new LexerToken(token, pos-1, -10, Token.QUOTED_STRING));
                continue;
            }
            if( "\"".equals(token) ) {
            	ret.add(new LexerToken(token, pos-1, -11, Token.DQUOTED_STRING));
                continue;
            }
            if( operation.contains(token) ) {
            	ret.add(new LexerToken(token, pos-1, pos, Token.OPERATION));
                continue;
            }
            if( ws.contains(token) ) {
            	ret.add(new LexerToken(token, pos-1, pos, Token.WS));
                continue;
            }
            if( '0'<=token.charAt(0) && token.charAt(0)<='9' ) {
            	// FIXME: "1e01" is treated as "digits", "1e+01" is treated as "digits '+' digits" 
            	// This seems to be a minor bug -- the containing expressions are OK  
            	ret.add(new LexerToken(token, pos-token.length(), pos, Token.DIGITS));
                continue;
            } 
        	ret.add(new LexerToken(token, pos-token.length(), pos, Token.IDENTIFIER));      
            
        }
        
        return ret;
    }
    public static void main(String[] args) throws Exception {
    	
    	if( false ) { // compare with canonical RegExp method
    		String input = Util.readFile(
				"C:/raptor_trunk/db/src/oracle/dbtools/parser/test.sql"
			);
    		long t1 = System.currentTimeMillis();
        	List<LexerToken> out1 = parse(input,true);
    		long t2 = System.currentTimeMillis();
    		LexerToken.print(out1);
    		System.out.println("Lexer time = "+(t2-t1)); // (authorized)
    		System.out.println(out1.size()); // (authorized)
        	/*List<LexerToken> out2 = legacyParse((CharSequence)input);
        	for( int i = 0; i < out1.size(); i++ ) {
        		if( !out1.get(i).content.equals(out2.get(i).content)
        			||	out1.get(i).begin !=out2.get(i).begin
        			||	out1.get(i).end !=out2.get(i).end
        		) {
        			System.out.println("i="+i); // (authorized)
        			System.out.println(out1.get(i)); // (authorized)
        			System.out.println(out2.get(i)); // (authorized)
        			return;
        		}
        	}
        	return;*/
   		
    	} else {
    	
    		String input = 
    			//"a \"b\n"+ 
    			//"'select ''Y'' from T '   ''''\n" +
    			//" ' multiline \n literal' 123 a1$ x# \n" +
    			//" /* 3s5d7 \n asdsa multiline comment*/ \n" +
    			//"a 3a --a3 ss 5<7  \n" +
    			"  3.5 /* ** / -- ' ' \" \" */\n" +
    			"'/* */'\"/*--*/\"   abc\n" +
    			"'Cannot find the key_id for key: \"'||p_key_name||'\" for table \"'||"+
    			"";

    		List<LexerToken> out = parse(input,true);
    		LexerToken.print(out);
    		//System.out.print(out); // (authorized)
    	}
    }

	public static LinkedList<LexerToken> parse( String input ) {
		return parse(input, false);
	}
	public static LinkedList<LexerToken> parse( String input, boolean keepWSandCOMMENTS ) {
		//return parse((CharSequence)input);
		LinkedList<LexerToken> ret = new LinkedList<LexerToken>();
		LexerToken last = null;
		for( LexerToken token : tokenize(input) ) {
			if( token.type == Token.QUOTED_STRING ) {   // glue strings together
				if( last != null && last.type == Token.QUOTED_STRING ) {
					last.content = last.content + token.content;
					last.end = token.end;
					continue;
				}
			}
		    if( keepWSandCOMMENTS || token.type != Token.WS && token.type != Token.COMMENT && token.type != Token.LINE_COMMENT )
				ret.add(token);
		    last = token;
		}
		return ret;
	}
	
	public static int scanner2parserOffset( List<LexerToken> src, int start ) {
		int offset = -1;
    	for( LexerToken t : src ) {
    		offset++;
    		if( t.end > start ) 
    			break;
    	}
    	return offset;
	}
}
