package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

@Deprecated //TODO [2033] debug only
public class PersistableTypeGuard {
	
	private final static String[] persistableObjects = {
			"SimpleObject",
			"Customer"
	};

	public static void post(ObjectAdapter actualAdapter) {
		
		String debug = "" + actualAdapter.getOid();
		
		if(isPersistable(debug)) {
            System.out.println("!!! [ObjectAdapter] "+debug);
            if(debug.contains("!")) {
                throw _Exceptions.unexpectedCodeReach();
            }            
        }
		
	}
	
	private static boolean isPersistable(String input) {
		for(String x : persistableObjects) {
			if(input.contains("."+x+":")) {
				return true;
			}
		}
		return false;
	}
	

}
