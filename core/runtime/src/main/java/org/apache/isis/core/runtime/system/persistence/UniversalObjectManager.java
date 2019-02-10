package org.apache.isis.core.runtime.system.persistence;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.net.URISyntaxException;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.UniversalOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.contextmanger.AuthorityDescriptor;
import org.apache.isis.core.runtime.contextmanger.ContextManager;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;

import lombok.Getter;
import lombok.Value;
import lombok.val;

/**
 * @since 2.0.0-M3
 */
public interface UniversalObjectManager {

	static UniversalObjectManager current() {
		//TODO [2033] properly register with _Context ?
		return UniversalObjectManagerSample.INSTANCE;
	}

	// -- INTERFACE
	
	OidEncoder getOidEncoder();
	OidDecoder getOidDecoder();
	
	AuthorityDescriptor authorityForElseFail(ObjectSpecId specId);
	
	Stream<ObjectAdapter> resolve(Stream<UniversalOid> uoids);
	ObjectAdapter resolve(UniversalOid uoid);

	// -- RESULT CONTAINER 
	
	@Value(staticConstructor="of")
	static final class ResolveResult {
		@Getter private final UniversalOid uoid; 
		@Getter private final ManagedObject managedObject;
		
		public ObjectAdapter toObjectAdapter(IsisSession isisSession) {
			val pojo = managedObject.getPojo();
			return pojo!=null 
					? PojoAdapter.of(pojo, uoid, isisSession)
							: null;
		}
		
	}
	
	// -- OID ENCODER / DECODER
	
	static interface OidEncoder {
		String encodeToString(@Nullable UniversalOid oid);

		default String encodeToStringWithLegacySupport(@Nullable Oid oid) {
			if (oid == null) {
	            return null;
	        }
			if(oid instanceof UniversalOid) {
	    		return encodeToString((UniversalOid) oid);
			}
			// legacy support ...
			_Probe.warnNotImplementedYet("instead we should convert a concrete RootOid to a UniversalOid");
			// FIXME [2033] instead we should convert a concrete RootOid to a UniversalOid
	    	return oid.enString();
		}
	}
	
	static interface OidDecoder {
		_Either<UniversalOid, URISyntaxException> decodeFromString(String encoded);
		
		default UniversalOid decodeFromStringElseFail(String encoded) {
			val uoidOrError = decodeFromString(encoded);
	    	if(uoidOrError.isRight()) {
	    		
	    		// TODO [2033] legacy support if required could be ...
	    		// final RootOid rootOid = RootOid.deStringEncoded(value);
	    		
	    		throw _Exceptions.unrecoverable("Oid decoding failed.", uoidOrError.rightIfAny());	
	    	}
			return uoidOrError.leftIfAny();
		}
	}
	
	// --
	
	static class Codecs {
		
		private static class UniversalOidEncoder implements OidEncoder {

			@Override
			public String encodeToString(@Nullable UniversalOid oid) {
				if (oid == null) {
		            return null;
		        }
		        return oid.enString();
			}
			
		}
		
		private static class UniversalOidDecoder implements OidDecoder {

			@Override
			public _Either<UniversalOid, URISyntaxException> decodeFromString(String encoded) {
				return UniversalOid.parseUri(encoded);
			}
			
		}
		
	}
	
	// -- SAMPLE - TODO [2033] just for proof of concept, needs refinement
	
	static class UniversalObjectManagerSample implements UniversalObjectManager {

		static UniversalObjectManagerSample INSTANCE = new UniversalObjectManagerSample();
		
		final _Lazy<ContextManager> contextManager = _Lazy.of(()->
			IsisContext.getServiceRegistry().lookupServiceElseFail(ContextManager.class));
		
		final _Probe probe = _Probe.unlimited().label("UniversalObjectManagerSample");
		
			
		@Override
		public Stream<ObjectAdapter> resolve(Stream<UniversalOid> uoids) {
			val contextManager = this.contextManager.get();
			val isisSession = IsisSession.currentIfAny();
			
			probe.println("resolve multiple ...");
			
			return stream(uoids)
					.flatMap(uoid->{
						
						probe.println(1, "resolving %s", uoid.enString());
						
						val instance = contextManager.resolve(uoid);
						return instance.stream()
						.map(managedObject->ResolveResult.of(uoid, managedObject)
								.toObjectAdapter(isisSession))
						.filter(_NullSafe::isPresent);
					})
					;
		}

		@Override
		public ObjectAdapter resolve(UniversalOid uoid) {
			val contextManager = this.contextManager.get();
			val instance = contextManager.resolve(uoid);
			
    		if(instance.isResolvable()) {
    			val managedObject = instance.get();
    			val isisSession = IsisSession.currentIfAny();
    			
    			return ResolveResult.of(uoid, managedObject).toObjectAdapter(isisSession);
    			
    		} else if(instance.isAmbiguous()) {
    			
    			//FIXME [2033] handle ambiguity
    			throw _Exceptions.notImplemented();
    			
    		} else {
    			
    			//FIXME [2033] handle no such element
    			throw _Exceptions.notImplemented();
    		}
			
		}

		@Override
		public OidEncoder getOidEncoder() {
			return new Codecs.UniversalOidEncoder();
		}

		@Override
		public OidDecoder getOidDecoder() {
			return new Codecs.UniversalOidDecoder();
		}

		@Override
		public AuthorityDescriptor authorityForElseFail(ObjectSpecId specId) {
			
			// given the specId, try to resolve 'containerType', 'contextType' and 'contextId'
			val contextManager = this.contextManager.get();
			val isisSession = IsisSession.currentIfAny();
			val spec = isisSession.getSpecificationLoader().lookupBySpecId(specId);
			
			val authority = contextManager.authorityFor(spec).orElseThrow(()->{
				val errMsg = String.format(
						"UniversalObjectManager cannot find the authority that handles specId '%s'.", 
						specId.asString());
				
				return _Exceptions.unrecoverable(errMsg);
			});
			
			return authority;
		}
		
	}
	

}
