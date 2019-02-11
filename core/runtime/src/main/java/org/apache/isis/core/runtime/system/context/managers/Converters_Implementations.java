package org.apache.isis.core.runtime.system.context.managers;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.UniversalOid;
import org.apache.isis.core.runtime.system.context.managers.Converters.FromUriConverter;
import org.apache.isis.core.runtime.system.context.managers.Converters.ToUriConverter;

import lombok.val;

/**
 * 
 * @since 2.0.0-M3
 *
 */
final class Converters_Implementations {
	
	private static class FromUriConverterDefault implements FromUriConverter {

		@Override
		public String encodeToString(URI objectUri) {
			if (objectUri == null) {
	            return null;
	        }
			return objectUri.toString();
		}
		
		@Override
		public String encodeToBase64(URI objectUri) {
			if (objectUri == null) {
	            return null;
	        }
			
			val raw = _Strings.toBytes(encodeToString(objectUri), UTF_8);
			val encoded = _Bytes.asUrlBase64.apply(raw);
			return _Strings.ofBytes(encoded, UTF_8);
		}
		

		@Override
		public RootOid toRootOid(URI objectUri) {
			if (objectUri == null) {
	            return null;
	        }
			
			val uoid = UniversalOid.of(objectUri);
			val specId = uoid.getObjectSpecId();
			val identifier = uoid.getIdentifier();
			val versionEncoded = uoid.getVersion();
			
			//FIXME [2033] no version support
			return Oid.Factory.persistentOf(specId, identifier);
		}
		
	}
	
	private static class ToUriConverterDefault implements ToUriConverter {

		@Override
		public _Either<URI, URISyntaxException> decodeFromString(String uriString) {
				
			if(!_URI.isUoid(uriString)) {
				
				val ex = new URISyntaxException(uriString, "Does not match _URI#isUoid.");
				return _Either.right(ex);	
			}
			
			try {
				val uri = new URI(uriString);
				
				return _Either.left(uri);
				
			} catch (URISyntaxException ex) {
			
				return _Either.right(ex);
			}
			
		}
		
		@Override
		public _Either<URI, URISyntaxException> decodeFromBase64(String base64) {
			
			val base64Bytes = _Strings.toBytes(base64, UTF_8);
			val plainBytes = _Bytes.ofUrlBase64.apply(base64Bytes);
			val plain = _Strings.ofBytes(plainBytes, UTF_8);
			
			return decodeFromString(plain);
		}

		@Override
		public URI toURI(RootOid rootOid) {
			
			val specId = rootOid.getObjectSpecId();
			val identifier = rootOid.getIdentifier();
			val version = rootOid.getVersion();
			
			val objectManager = UniversalObjectManager.current();
			val authority = objectManager.authorityForElseFail(specId);
			
			val uriBuilder = authority.toUoidDtoBuilder(specId)
					.query(identifier);
			
			if(version!=null) {
				uriBuilder.fragment(version.enString());
			}
			
			return uriBuilder
					.build()
					.toURI();
		}
		
	}

	// -- STATIC INSTANCES
	
	final static FromUriConverter fromUriConverter = new FromUriConverterDefault();
	final static ToUriConverter toUriConverter = new ToUriConverterDefault();
	
}
