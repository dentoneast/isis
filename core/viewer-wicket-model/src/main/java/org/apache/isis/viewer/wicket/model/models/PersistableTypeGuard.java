package org.apache.isis.viewer.wicket.model.models;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

@Deprecated //TODO [2033] debug only
public class PersistableTypeGuard {
	
	private final static String[] persistableObjects = {
			"SimpleObject",
			"Customer"
	};

	public static void post(ObjectAdapterMemento adapterMemento) {
		
		String debug = "" + adapterMemento;
		
        if(isPersistable(debug)) {
            System.out.println("!!! [ObjectAdapterMemento] "+debug);
            if(debug.contains("!")) {
                throw _Exceptions.unexpectedCodeReach();
            }            
        }
		
	}

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
