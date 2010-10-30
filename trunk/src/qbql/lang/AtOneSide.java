package qbql.lang;

import qbql.lattice.Database;
import qbql.lattice.Relation;

public class AtOneSide {
    public static Relation x1_y1_x2_y2_xa_ya_xb_yb( int x1, int y1, int x2, int y2, int xa, int ya, int xb, int yb ) {
    	int vx = x2-x1; 
    	int vy = y2-y1; 
    	int ax = xa-x1; 
    	int ay = ya-y1; 
    	int bx = xb-x1; 
    	int by = yb-y1; 
    	int pa = vx*ay - vy*ax;
    	int pb = vx*by - vy*bx;
        if( pa*pb >= 0 )
            return Database.R01;
        else
            return Database.R00;
    }
}
