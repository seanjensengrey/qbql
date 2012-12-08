package qbql.deduction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import qbql.symbolic.Expr;
import qbql.util.Util;

public class Theory {
    List<Eq> assertions = new LinkedList<Eq>();
    
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for( Eq e1 : assertions )
            ret.append(e1.toString()+'\n');
        return ret.toString();
    }

    public void add( Eq eq ) {
        if( eq.size() == 0 )
            return;
        Eq add = eq;
        Eq remove = null;
        for( Eq e : assertions )
            if( e.equals(eq) ) {
                add = Eq.merge(e, eq); 
                remove = e;
            }
        if( remove != null )
            assertions.remove(remove);
        assertions.add(add);        
    }
    
    Theory assign( Set<String> variables ) {
        Theory ret = new Theory();
        String[] vars = variables.toArray(new String[0]);
        
        for( Eq eq : assertions ) {
            Set<String> allVars = new HashSet<String>();
            for( Expr ge : eq.expressions )
                allVars.addAll(ge.variables());
            
            int[] indexes = new int[allVars.size()];
            for( int i = 0; i < indexes.length; i++ )
                indexes[i] = 0;
            do {
                Eq tmp = eq;
                int var = 0;
                for( String variable : allVars.toArray(new String[0]) ) { 
                    tmp = tmp.substitute(variable,Prover.compose(null,"!"+vars[indexes[var++]],null));
                } 
                for( String variable : vars )  
                    tmp = tmp.substitute("!"+variable,Prover.compose(null,variable,null));
                ret.add(tmp);
                
            } while( Util.next(indexes,variables.size()) );
            
        }
        
        
        return ret;
    }
    
    private boolean step( int i, int j ) {
        int before = assertions.size();
        Eq e1 = assertions.get(i);
        Eq e2 = assertions.get(j);
        Eq o = e1.leverage(e2);
        if( o.size() == 0 )
            return true;
        add(Eq.merge(o, e1));
        return assertions.size() == before;
    }
    
    void step() {
        for( int i = 0; i < assertions.size(); i++ ) 
            for( int j = 0; j < assertions.size(); j++ ) 
                if( !step(i,j) )
                    return;
                    
    }

}
