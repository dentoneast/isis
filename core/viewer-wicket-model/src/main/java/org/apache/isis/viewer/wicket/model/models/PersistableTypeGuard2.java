package org.apache.isis.viewer.wicket.model.models;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.PersistableTypeGuard;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

@Deprecated //TODO [2033] debug only
public class PersistableTypeGuard2 extends PersistableTypeGuard {
	
	private final static _Probe probe = _Probe.unlimited().label("PersistableTypeGuard2");
	
	public static void instate(ObjectAdapterMemento adapterMemento) {
		String magicString = "" + adapterMemento;
		instate(magicString, probe, "ObjectAdapterMemento");
	}
	

}
