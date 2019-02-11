package org.apache.isis.core.runtime.system.context.managers.cdi;

import java.net.URI;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
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

	private final static _Probe probe = _Probe.unlimited().label("CDIContextManager");
	
	@Inject SpecificationLoader specLoader;
	@Inject ServiceRegistry serviceRegistry;
	
	@PostConstruct
	public void init() {
		//
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
	public Instance<ManagedObject> resolve(ObjectSpecId specId, URI objectUri) {
		
		val spec = specLoader.lookupBySpecId(specId);
		val id = objectUri.getQuery(); 
		//TODO [2033] future extension ... to refine, convert id to array of Annotations
		val qualifiers = _Constants.emptyAnnotations;
		
		val bean = serviceRegistry.getManagedBean(spec.getCorrespondingClass(), qualifiers);
		
		val managedObject = SimpleManagedObject.of(spec, bean);
		
		return _CDI.InstanceFactory.singleton(managedObject);
	}

	@Override
	public boolean recognizes(ObjectSpecification spec) {
		return serviceRegistry.isRegisteredService(spec.getCorrespondingClass());
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

