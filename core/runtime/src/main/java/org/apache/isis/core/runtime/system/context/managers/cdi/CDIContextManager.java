package org.apache.isis.core.runtime.system.context.managers.cdi;

import java.net.URI;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjectState;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.context.managers.AuthorityDescriptor;
import org.apache.isis.core.runtime.system.context.managers.ContextHandler;

import lombok.val;

/**
 * @since 2.0.0-M3
 */
@Singleton
public class CDIContextManager implements ContextHandler {

	private final static _Probe probe = _Probe.unlimited().label("CDIContextManager")
			.silence();
	
	@Inject SpecificationLoader specLoader;
	@Inject ServiceRegistry serviceRegistry;
	
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
		
		val spec = managedObject.getSpecification();
		
		probe.println("uriOf spec='%s'", 
        		spec.getSpecId().asString());
    		
		return DEFAULT_AUTHORITY.toUoidDtoBuilder(spec.getSpecId())
				.query(SystemConstants.SERVICE_IDENTIFIER)
				.build()
				.toURI();
	}

	@Override
	public Bin<ManagedObject> resolve(ObjectSpecId specId, URI objectUri) {
		
		val spec = specLoader.lookupBySpecId(specId);
		val id = objectUri.getQuery(); 
		//TODO [2033] future extension ... to refine, convert id to qualifiers
		val qualifiers = _Constants.emptyAnnotations;
		
		val bin = serviceRegistry.select(spec.getCorrespondingClass(), qualifiers);
		
		probe.println("resolve spec='%s' -> '%s'", 
        		spec.getSpecId().asString(),
        		bin);
		
		return bin.map(beanPojo->SimpleManagedObject.of(spec, beanPojo));
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
		return serviceRegistry.isRegisteredBean(spec.getCorrespondingClass());
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
			AuthorityDescriptor.of(ContainerType.cdi, ContextType.beans, null);


	
}

