package org.apache.isis.core.runtime.system.context.managers;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.val;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public final class Converters {
	
	public static FromUriConverter fromUriConverter() {
		return Converters_Implementations.fromUriConverter;
	}

	public static ToUriConverter toUriConverter() {
		return Converters_Implementations.toUriConverter;
	}
	
	// -- FROM URI CONVERTER
	
	public static interface FromUriConverter {
		String encodeToString(@Nullable URI objectUri);
		String encodeToBase64(@Nullable URI objectUri);
		RootOid toRootOid(@Nullable URI objectUri);
	}
	
	// -- TO URI CONVERTER
	
	public static interface ToUriConverter {
		
		_Either<URI, URISyntaxException> decodeFromString(String uriString);
		_Either<URI, URISyntaxException> decodeFromBase64(String base64EncodedUriString);
		
		default URI decodeFromStringElseFail(String encoded) {
			val uriOrError = decodeFromString(encoded);
	    	if(uriOrError.isRight()) {
	    		throw _Exceptions.unrecoverable("Object-URI decoding failed.", uriOrError.rightIfAny());	
	    	}
			return uriOrError.leftIfAny();
		}

		default URI toURI(ObjectSpecId specId, String identifier) {
			val objectManager = UniversalObjectManager.current();
			val authority = objectManager.authorityForElseFail(specId);
			val uri = authority.toUoidDtoBuilder(specId)
					.query(identifier)
					.build()
					.toURI();		

			return uri; 
		}
		
		URI toURI(RootOid rootOid);

		default URI toURI(Bookmark bookmark) {
        	
			val specId = ObjectSpecId.of(bookmark.getObjectType());
			val identifier = bookmark.getIdentifier();
			//val state = Oid_State.from(bookmark); // ignored
			//val version = Version.empty(); // ignored
			
			return toURI(specId, identifier);
		}
		
	}
		
	// --
	

	
	
}
