package org.apache.isis.core.metamodel.adapter.oid;

import java.net.URI;

import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public interface UniversalOid /*extends RootOid*/ {

	URI getObjectUri();

	// -- FROM (ROOT) OID
	
	ObjectSpecId getObjectSpecId();
	String getIdentifier();
	Version getVersion();
	
	// -- FACTORIES
	
	static UniversalOid of(URI uri) {
		return Oid.Factory.universal(uri);
	}
	
}
