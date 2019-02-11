package org.apache.isis.core.runtime.system.context.managers.builtin;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
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
public class ViewModelContextManager implements ContextHandler {

	private final static _Probe probe = _Probe.unlimited().label("ViewModelContextManager");

	@Inject SpecificationLoader specLoader;
	@Inject ServiceInjector serviceInjector;

	@PostConstruct
	public void init() {
		//
	}

	@Override
	public URI uriOf(ManagedObject managedObject) {

		val pojo = managedObject.getPojo();
		val spec = managedObject.getSpecification();

		val viewModelFacet = spec.getFacet(ViewModelFacet.class);
		val serialized = viewModelFacet.memento(pojo);

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

		probe.println("resolve spec='%s', serialized='%s'", 
				spec.getSpecId().asString(),
				serialized);

		val viewModelPojo = deserializeViewModel(spec, serialized);

		val managedObject = SimpleManagedObject.of(spec, viewModelPojo);

		return _CDI.InstanceFactory.singleton(managedObject);
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
		return spec.isViewModel();
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
			AuthorityDescriptor.of(ContainerType.builtin, ContextType.beans, "vm");

	private Object deserializeViewModel(final ObjectSpecification spec, final String serialized) {
		final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
		if(facet == null) {
			throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " 
					+ spec.getFullIdentifier());
		}

		final Object viewModelPojo;
		if(facet.getRecreationMechanism().isInitializes()) {
			viewModelPojo = instantiateAndInjectServices(spec);
			facet.initialize(viewModelPojo, serialized);
		} else {
			viewModelPojo = facet.instantiate(spec.getCorrespondingClass(), serialized);
		}
		return viewModelPojo;
	}
	
	private Object instantiateAndInjectServices(final ObjectSpecification objectSpec) {

        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        if (correspondingClass.isArray()) {
            return Array.newInstance(correspondingClass.getComponentType(), 0);
        }

        final Class<?> cls = correspondingClass;
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IsisException("Cannot create an instance of an abstract class: " + cls);
        }

        final Object newInstance;
        try {
            newInstance = cls.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IsisException("Failed to create instance of type " + objectSpec.getFullIdentifier(), e);
        }

        serviceInjector.injectServicesInto(newInstance);
        return newInstance;

    }
	
}

