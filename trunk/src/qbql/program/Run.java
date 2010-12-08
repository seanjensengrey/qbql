package qbql.program;

import qbql.induction.ExprGen;
import qbql.lattice.Database;
import qbql.lattice.Program;
import qbql.util.Profile;
import qbql.util.Util;

public class Run {
    
    public static void main( String[] args ) throws Exception {
        String prg = null;
        //Profile.sample(10000, 30);
        if( args.length == 1 ) {
            prg = Util.readFile(args[0]);
        } else {
            //prg = Util.readFile(Run.class,"strings.prg");
            prg = Util.readFile(Run.class,"current.prg");     
        }
    	Database db = new Database("qbql.lang");
		final Program prog = new Program(db);
		prog.run(prg);       
    }

}