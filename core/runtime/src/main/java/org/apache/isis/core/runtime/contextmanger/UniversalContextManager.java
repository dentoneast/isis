package org.apache.isis.core.runtime.contextmanger;

import static org.apache.isis.commons.internal.base._With.requires;

import java.net.URI;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@Singleton
public class UniversalContextManager implements ContextManager {

	@Inject Instance<ContextHandler> contextHandlers;
	
	@Override
	public URI identifierOf(ManagedObject managedObject) {
		
		requires(managedObject, "managedObject");
		
		val resolver = resolverFor(managedObject.getSpecification());
		if(resolver==null) {
			val msg = String.format(
					"Could not find a ContextHandler that recognizes managedObject of type %s.", managedObject);
			throw _Exceptions.unrecoverable(msg); 
		}
		
		val uri = resolver.identifierOf(managedObject);
		
		return uri;
		
	}

	@Override
	public Instance<ManagedObject> resolve(ObjectSpecId spec, URI identifier) {
		
		requires(identifier, "identifier");
		
		val instance = contextHandlers.stream()
		.filter(handler->handler.recognizes(identifier))
		.findFirst()
		.map(handler->handler.resolve(spec, identifier))
		.orElseThrow(()->{
			val msg = String.format(
					"Could not find a ContextHandler that recognizes identifier URI %s.", identifier);
			return _Exceptions.unrecoverable(msg);
		}); 
		
		return instance;
	}

	@Override
	public ManagedObjectResolver resolverFor(ObjectSpecification objSpec) {
		
		requires(objSpec, "objSpec");
		
		return contextHandlers.stream()
		.filter(handler->handler.recognizes(objSpec))
		.findFirst()
		.orElse(null);
	}


	
	
}
