package domainapp.modules.spring;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.UUID;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.contextmanger.ContextHandler;
import org.springframework.context.ApplicationContext;

import domainapp.modules.spring.dom.customer.Customer;
import lombok.val;

@Singleton
public class SpringDataJPADemoHandler implements ContextHandler {
	
	private final static _Probe probe = _Probe.unlimited().label("SpringDataJPADemoHandler");
	
	@Inject ApplicationContext springContext;
	@Inject SpecificationLoader specLoader;

	@Override
	public URI identifierOf(ManagedObject managedObject) {
    	try {
			return identifierForCustomer(managedObject.getPojo(), managedObject.getSpecification());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw _Exceptions.unrecoverable(e);
		}
	}

	@Override
	public Instance<ManagedObject> resolve(ObjectSpecId specId, URI identifier) {
		
		// FIXME [2033] this is just a PoC
		
		long id = Long.parseLong(identifier.getQuery());
		
		val em = springContext.getBean(EntityManager.class);
		
		val customer = em.find(Customer.class, id);
		val spec = specLoader.lookupBySpecId(specId);
		
		val managedObject = SimpleManagedObject.of(spec, customer);
		
		return _CDI.InstanceFactory.singleton(managedObject);
	}

	@Override
	public boolean recognizes(URI uri) {
		return uri.toString().startsWith("uoid://entities@spring");
	}

	@Override
	public boolean recognizes(ObjectSpecification objSpec) {
		// FIXME [2033] this is just a PoC
		return objSpec.getCorrespondingClass().isAnnotationPresent(Entity.class);
	}
	
	// -- HELPER
	
	private URI identifierForCustomer(Object pojo, ObjectSpecification spec) 
    		throws NoSuchMethodException, SecurityException, IllegalAccessException, 
    		IllegalArgumentException, InvocationTargetException {
    	
    	val idGetter = pojo.getClass().getMethod("getId", _Constants.emptyClasses);
    	final Long id = (Long)idGetter.invoke(pojo, _Constants.emptyObjects);
    	final String identifier = (id!=null && id>0) 
    			? "" + id
    					: UUID.randomUUID().toString();
    		
		return _URI.uoidBuilder()
				.containerType(ContainerType.spring)
				.contextType(ContextType.entities)
				.contextId(null)
				.path("/"+spec.getSpecId().asString()+"/")
				.query(identifier)
				.build()
				.toURI();
    }
	

}