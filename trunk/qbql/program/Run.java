package qbql.program;

import qbql.lattice.Database;
import qbql.util.Util;

public class Run {
    public static void main( String[] args ) throws Exception {
        String db = Util.readFile(Run.class,"strings.db");
        String prg = Util.readFile(Run.class,"strings.prg");
        Database.run(prg, db);    
    }

}
