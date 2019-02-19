package org.apache.isis.core.runtime.system.context.managers.builtin;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.PersistableTypeGuard;
import org.apache.isis.core.metamodel.spec.ManagedObject;
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
@Vetoed // TODO [2033] not required
public class MixinContextManager implements ContextHandler {

	private final static _Probe probe = _Probe.unlimited().label("MixinContextManager");

	@Inject SpecificationLoader specLoader;
	@Inject ServiceInjector serviceInjector;

	@PostConstruct
	public void init() {
		//
	}
	
	@Override
	public ManagedObjectState stateOf(ManagedObject managedObject) {
		return ManagedObjectState.not_Persistable;
	}

	@Override
	public URI uriOf(ManagedObject managedObject) {

		val pojo = managedObject.getPojo();
		val spec = managedObject.getSpecification();
		
		if(!recognizes(spec)) { //TODO [2033] remove guard once known to be stable
			throw _Exceptions.unexpectedCodeReach();
		}

		probe.println("uriOf spec='%s'", 
				spec.getSpecId().asString());

		return DEFAULT_AUTHORITY.toUoidDtoBuilder(spec.getSpecId())
				.query("")
				.build()
				.toURI();
	}

	@Override
	public Instance<ManagedObject> resolve(ObjectSpecId specId, URI objectUri) {
		
		throw _Exceptions.notImplemented();

//		val serialized = objectUri.getQuery();
//		val spec = specLoader.lookupBySpecId(specId);
//		
//		if(!recognizes(spec)) { //TODO [2033] remove guard once known to be stable
//			throw _Exceptions.unexpectedCodeReach();
//		}
//
//		probe.println("resolve spec='%s'", 
//				spec.getSpecId().asString());
//
//		val viewModelPojo = deserializeViewModel(spec, serialized);
//
//		val managedObject = SimpleManagedObject.of(spec, viewModelPojo);
//
//		return _CDI.InstanceFactory.singleton(managedObject);
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
		
		PersistableTypeGuard.instate(spec);
		
		return spec.isMixin();
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
			AuthorityDescriptor.of(ContainerType.builtin, ContextType.beans, "mx");

//	private Object deserializeViewModel(final ObjectSpecification spec, final String serialized) {
//		final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
//		if(facet == null) {
//			throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " 
//					+ spec.getFullIdentifier());
//		}
//
//		final Object viewModelPojo;
//		if(facet.getRecreationMechanism().isInitializes()) {
//			viewModelPojo = instantiateAndInjectServices(spec);
//			facet.initialize(viewModelPojo, serialized);
//		} else {
//			viewModelPojo = facet.instantiate(spec.getCorrespondingClass(), serialized);
//		}
//		return viewModelPojo;
//	}
	
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
        	
        	val errMsg = String.format("Failed to create instance of type '%s'", 
        			objectSpec.getFullIdentifier()); 
        	
            throw new IsisException(errMsg, e);
        }

        serviceInjector.injectServicesInto(newInstance);
        return newInstance;

    }
	
}

