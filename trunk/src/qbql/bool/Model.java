package qbql.bool;

/**
 * Mace4-inspired tool optimized and specialized for Boolean Algebra embeddings (e.g. BA with closure)
 * There is only one model of size 2^n (up to isomorphism) which cuts down model checking time significantly
 * One has to shuffle elements in the additional operators only 
 * @author Dim
 */
public class Model {
    
    public static void main( String[] args ) {
        for( int dim = 2; dim < 5; dim++ ) {
            long t1 = System.currentTimeMillis();
            long size = 1L << dim;
            System.out.print("size = "+size);
            UnaryOperator oper = new UnaryOperator(dim);
            do {
                //System.out.println(oper.toString());
                oper = oper.next();
            } while( oper != null );
            System.out.println(" ("+(System.currentTimeMillis()-t1)+" ms)");
        }
    }
    
}
