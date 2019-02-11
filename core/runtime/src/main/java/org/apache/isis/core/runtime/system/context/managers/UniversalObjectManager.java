package org.apache.isis.core.runtime.system.context.managers;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.net.URI;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.UniversalOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
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
	
	AuthorityDescriptor authorityForElseFail(ObjectSpecId specId);
	ContextHandler contextHandlerForElseFail(ObjectSpecification spec);
	
	
	Stream<ObjectAdapter> resolve(Stream<URI> objectUris);
	ObjectAdapter resolve(URI objectUri);

	// -- RESULT CONTAINER 
	
	@Value(staticConstructor="of")
	static final class ResolveResult {
		@Getter private final URI objectUri; 
		@Getter private final ManagedObject managedObject;
		
		public ObjectAdapter toObjectAdapter(IsisSession isisSession) {
			val pojo = managedObject.getPojo();
			if(pojo==null) {
				return null;
			}
			val converter = Converters.fromUriConverter();
			val rootOid = converter.toRootOid(objectUri);
			return PojoAdapter.of(pojo, rootOid, isisSession);
		}
		
	}
	
	
	
	// -- SAMPLE - TODO [2033] just for proof of concept, needs refinement
	
	static class UniversalObjectManagerSample implements UniversalObjectManager {

		static UniversalObjectManagerSample INSTANCE = new UniversalObjectManagerSample();
		
		final _Lazy<ContextManager> contextManager = _Lazy.of(()->
			IsisContext.getServiceRegistry().lookupServiceElseFail(ContextManager.class));
		
		final _Probe probe = _Probe.unlimited().label("UniversalObjectManagerSample");
		
			
		@Override
		public Stream<ObjectAdapter> resolve(Stream<URI> objectUris) {
			val contextManager = this.contextManager.get();
			val isisSession = IsisSession.currentIfAny();
			
			probe.println("resolve multiple ...");
			
			return stream(objectUris)
					.flatMap(objectUri->{
						
						probe.println(1, "resolving %s", objectUri);
						
						val instance = contextManager.resolve(UniversalOid.of(objectUri));
						return instance.stream()
						.map(managedObject->ResolveResult.of(objectUri, managedObject))
						.filter(_NullSafe::isPresent)
						.map(resolveResult->resolveResult.toObjectAdapter(isisSession))
						.filter(_NullSafe::isPresent);
					})
					;
		}

		@Override
		public ObjectAdapter resolve(URI objectUri) {
			val contextManager = this.contextManager.get();
			val instance = contextManager.resolve(UniversalOid.of(objectUri));
			
    		if(instance.isResolvable()) {
    			val managedObject = instance.get();
    			val isisSession = IsisSession.currentIfAny();
    			
    			return ResolveResult.of(objectUri, managedObject).toObjectAdapter(isisSession);
    			
    		} else if(instance.isAmbiguous()) {
    			
    			//FIXME [2033] handle ambiguity
    			throw _Exceptions.notImplemented();
    			
    		} else {
    			
    			//FIXME [2033] handle no such element
    			throw _Exceptions.notImplemented();
    		}
			
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

		@Override
		public ContextHandler contextHandlerForElseFail(ObjectSpecification spec) {
			val contextManager = this.contextManager.get();
			return (ContextHandler) contextManager.resolverForIfAny(spec);
		}
		
	}
	

}
