package qbql.program;

import qbql.lattice.Database;
import qbql.util.Util;

public class Test {
    public static void main( String[] args ) throws Exception {
        String db = Util.readFile(Run.class,"Test.db");;
        String prg = Util.readFile(Run.class,"Test.prg"); ;
        Database.run(db, prg);    
    }

}
