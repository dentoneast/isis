package org.apache.isis.core.metamodel.adapter.oid;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.uri._URI;

import lombok.val;

public interface UniversalOid extends RootOid {

	URI universalId();

	static _Either<UniversalOid, URISyntaxException> parseUri(String uriString) {
		
		if(!_URI.isUoid(uriString)) {
			
			val ex = new URISyntaxException(uriString, "Does not match _URI#isUoid.");
			return _Either.right(ex);	
		}
		
		try {
			val uri = new URI(uriString);
			
			return _Either.left(Oid.Factory.universal(uri));
			
		} catch (URISyntaxException ex) {
		
			return _Either.right(ex);
		}
		
	}
	
}
