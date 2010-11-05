package qbql.lang;

import java.math.BigDecimal;

import qbql.index.NamedTuple;

public class Times {
    public static String getSymbolicName() {
    	return "x * y = z";
    }

    public static NamedTuple x_y_z( 
            Object x, Object y 
    ) {
        String[] columns = new String[]{"z"};
        if( x instanceof Integer & y instanceof Integer )
        	return new NamedTuple(columns,new Object[]{(Integer)x*(Integer)y});
        if( x instanceof Float & y instanceof Float )
        	return new NamedTuple(columns,new Object[]{(Float)x*(Float)y});
        if( x instanceof Double & y instanceof Double )
        	return new NamedTuple(columns,new Object[]{(Double)x*(Double)y});
        if( x instanceof BigDecimal & y instanceof BigDecimal )
        	return new NamedTuple(columns,new Object[]{((BigDecimal)x).multiply((BigDecimal)y)});
        throw new AssertionError("? * ?");
    }
    public static NamedTuple x_z_y( 
    		Object x, Object z 
    ) {
        String[] columns = new String[]{"y"};
        if( x instanceof Integer & z instanceof Integer )
        	return new NamedTuple(columns,new Object[]{(Integer)z/(Integer)x});
        if( x instanceof Float & z instanceof Float )
        	return new NamedTuple(columns,new Object[]{(Float)z/(Float)x});
        if( x instanceof Double & z instanceof Double )
        	return new NamedTuple(columns,new Object[]{(Double)z/(Double)x});
        if( x instanceof BigDecimal & z instanceof BigDecimal )
        	return new NamedTuple(columns,new Object[]{((BigDecimal)z).divide((BigDecimal)x)});
        throw new AssertionError("? / ?");
    }
    public static NamedTuple y_z_x( 
    		Object y, Object z 
    ) {
        String[] columns = new String[]{"x"};
        if( y instanceof Integer & z instanceof Integer )
        	return new NamedTuple(columns,new Object[]{(Integer)z/(Integer)y});
        if( y instanceof Float & z instanceof Float )
        	return new NamedTuple(columns,new Object[]{(Float)z/(Float)y});
        if( y instanceof Double & z instanceof Double )
        	return new NamedTuple(columns,new Object[]{(Double)z/(Double)y});
        if( y instanceof BigDecimal & z instanceof BigDecimal )
        	return new NamedTuple(columns,new Object[]{((BigDecimal)z).divide((BigDecimal)y)});
        throw new AssertionError("? / ?");
    }
}
