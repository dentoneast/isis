package org.apache.isis.core.runtime.system.context.managers.builtin;

import java.net.URI;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.managers.AuthorityDescriptor;
import org.apache.isis.core.runtime.system.context.managers.ContextHandler;

import lombok.val;

/**
 * @since 2.0.0-M3
 */
@Singleton
public class ValueContextManager implements ContextHandler {

	private final static _Probe probe = _Probe.unlimited().label("ValueContextManager");

	@Inject SpecificationLoader specLoader;
	@Inject UrlEncodingService urlEncodingService;
	@Inject SerializingAdapter serializingAdapter;

	@PostConstruct
	public void init() {
		//
	}

	@Override
	public URI uriOf(ManagedObject managedObject) {

		val value = managedObject.getPojo();
		val spec = managedObject.getSpecification();

		val serialized = newMemento().put("value", value).asString();

		probe.println("uriOf spec='%s', serialized='%s'", 
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

		probe.println("resolve spec='%s', serialized='%s'", 
				spec.getSpecId().asString(),
				serialized);

		val value = parseMemento(serialized).get("value", expectedType);

		val managedObject = SimpleManagedObject.of(spec, value);

		return _CDI.InstanceFactory.singleton(managedObject);
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
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
}

