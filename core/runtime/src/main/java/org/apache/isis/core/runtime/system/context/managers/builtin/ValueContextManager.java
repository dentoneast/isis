package org.apache.isis.core.runtime.system.context.managers.builtin;

import java.io.Serializable;
import java.net.URI;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.services.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.managers.AuthorityDescriptor;
import org.apache.isis.core.runtime.system.context.managers.ContextHandler;

import lombok.val;

/**
 * @since 2.0.0-M3
 */
@Vetoed // TODO [2033] not required, as long as ObjectAdapterMemento is handling values, 
// not sure whether we can remove this yet 
public class ValueContextManager implements ContextHandler {

	private final static _Probe probe = _Probe.unlimited().label("ValueContextManager");

	@Inject SpecificationLoader specLoader;
	
	private SerializingAdapter serializingAdapter;
	private UrlEncodingService urlEncodingService;

	@PostConstruct
	public void init() {
		urlEncodingService = new UrlEncodingServiceWithCompression();
		serializingAdapter = new SimpleSerializingAdapter();
	}
	
	@Override
	public ManagedObjectState stateOf(ManagedObject managedObject) {
		return ManagedObjectState.not_Persistable;
	}
	
	@Override
	public URI uriOf(ManagedObject managedObject) {

		val value = managedObject.getPojo();
		val spec = managedObject.getSpecification();

		val serialized = newMemento().put("val", value).asString();

		probe.println("uriOf spec='%s' -> serialized='%s'", 
				spec.getSpecId().asString(),
				serialized);

		return DEFAULT_AUTHORITY.toUoidDtoBuilder(spec.getSpecId())
				.query(serialized)
				.build()
				.toURI();
	}

	@Override
	public Instance<ManagedObject> resolve(ObjectSpecId specId, URI objectUri) {
		
		val serialized = objectUri.getQuery();
		val spec = specLoader.lookupBySpecId(specId);
		val expectedType = spec.getCorrespondingClass();

		probe.println("resolve spec='%s', uri='%s'", 
				spec.getSpecId().asString(),
				objectUri);
		
		//FIXME [2033] we expect a base64 encoded memento container, but do get plaintext
		probe.warnNotImplementedYet("we expect a base64 encoded memento container, "
				+ "but do get plaintext here: '%s'", serialized);
		
//		if("String".equals(expectedType.getSimpleName())) {
//			val value = serialized;
//			val managedObject = SimpleManagedObject.of(spec, value);
//			return _CDI.InstanceFactory.singleton(managedObject);
//		}

		try {
		
			val value = parseMemento(serialized).get("val", expectedType);
			val managedObject = SimpleManagedObject.of(spec, value);
			return _CDI.InstanceFactory.singleton(managedObject);
			
		} catch (Exception cause) {
			val errMsg = String.format("Failed to deserialize memento (container) holding expected type '%s' <- '%s'.",
					expectedType.getName(),
					serialized);
			throw _Exceptions.unrecoverable(errMsg, cause);
		}
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
		
		if(spec.isValue()) {
			probe.println("recognizes spec='%s'", spec);	
		}
		return spec.isValue();
	}

	@Override
	public boolean recognizes(URI uri) {
		return DEFAULT_AUTHORITY.matches(uri);
	}

	@Override
	public Optional<AuthorityDescriptor> authorityFor(ObjectSpecification spec) {
		if(!recognizes(spec)) {
			return Optional.empty();
		}
		return Optional.of(DEFAULT_AUTHORITY);
	}

	// -- HELPER

	private final static AuthorityDescriptor DEFAULT_AUTHORITY =
			AuthorityDescriptor.of(ContainerType.builtin, ContextType.beans, "val");
		
    private _Mementos.Memento newMemento(){
        return _Mementos.create(urlEncodingService, serializingAdapter);
    }

    private _Mementos.Memento parseMemento(String input){
        return _Mementos.parse(urlEncodingService, serializingAdapter, input);
    }
    
    private final static class SimpleSerializingAdapter implements SerializingAdapter {

		@Override
		public Serializable write(Object value) {
			if(value instanceof Serializable) {
				return (Serializable) value;
			}
			probe.warnNotImplementedYet("This SerializingAdapter can only handle values that are 'serializable'. "
					+ "Got '%s'.", ""+value);
			
			//throw _Exceptions.unrecoverable("This SerializingAdapter can only handle values that are 'serializable'.");
			return null;
		}

		@Override
		public <T> T read(Class<T> cls, Serializable value) {
			return _Casts.uncheckedCast(value);
		}
    	
    }
    
}

