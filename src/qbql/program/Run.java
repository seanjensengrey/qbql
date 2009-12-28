package qbql.program;

import qbql.lattice.Database;
import qbql.util.Util;

public class Run {
    
    public static void main( String[] args ) throws Exception {
        String db = null;
        String prg = null;
        if( args.length == 2 ) {
            db = Util.readFile(args[0]);
            prg = Util.readFile(args[1]);
        } else {
            //db = Util.readFile(Run.class,"strings.db");
            //prg = Util.readFile(Run.class,"strings.prg");
            //db = Util.readFile(Run.class,"Sims.db");
            //prg = Util.readFile(Run.class,"Sims.assertions");
            //prg = Util.readFile(Run.class,"mereologic.prg");
            //db = Util.readFile(Run.class,"Wittgenstein.db");
            //prg = Util.readFile(Run.class,"Wittgenstein.assertions");
            //db = Util.readFile(Run.class,"sets.db");
            //prg = Util.readFile(Run.class,"sets.prg");
            db = Util.readFile(Run.class,"Figure1.db");
            prg = Util.readFile(Run.class,"Figure1.prg");     
        }
        Database.run(db, prg);    
    }

}
