package qbql.bool;

/**
 * Mace4-inspired tool optimized and specialized for Boolean Algebra embeddings (e.g. BA with closure)
 * There is only one model of size 2^n (up to isomorphism) which cuts down model checking time significantly
 * One has to shuffle elements in the additional operators only 
 * @author Dim
 */
public class Model {
    
    public static void main( String[] args ) {
        for( int dim = 2; dim < 10; dim++ ) {
            long t1 = System.currentTimeMillis();
            long size = 1L << dim;
            UnaryOperator oper = new UnaryOperator(dim);
            int count = 0;
            do {
                //System.out.println(oper.toString());
                count++;
                oper = oper.next();
            } while( oper != null );
            System.out.print("models("+size+")="+count);
            System.out.println("      ("+(System.currentTimeMillis()-t1)+" ms)");
        }
    }
    
}
