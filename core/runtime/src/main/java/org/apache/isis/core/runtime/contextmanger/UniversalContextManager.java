package org.apache.isis.core.runtime.contextmanger;

import static org.apache.isis.commons.internal.base._With.requires;

import java.net.URI;
import java.util.Optional;

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
		
		val resolver = resolverForIfAny(managedObject.getSpecification());
		if(resolver==null) {
			val msg = String.format(
					"Could not find a ContextHandler that recognizes managedObject of type %s.", managedObject);
			throw _Exceptions.unrecoverable(msg); 
		}
		
		val uri = resolver.identifierOf(managedObject);
		
		return uri;
		
	}

	@Override
	public Instance<ManagedObject> resolve(ObjectSpecId specId, URI identifier) {
		
		requires(identifier, "identifier");
		
		val instance = contextHandlers.stream()
		.filter(handler->handler.recognizes(identifier))
		.findFirst()
		.map(handler->handler.resolve(specId, identifier))
		.orElseThrow(()->{
			val msg = String.format(
					"Could not find a ContextHandler that recognizes identifier URI %s.", identifier);
			return _Exceptions.unrecoverable(msg);
		}); 
		
		return instance;
	}

	@Override
	public Optional<ManagedObjectResolver> resolverFor(ObjectSpecification spec) {
		requires(spec, "spec");
		return contextHandlers.stream()
		.filter(handler->handler.recognizes(spec))
		.map(handler->(ManagedObjectResolver)handler)
		.findFirst();
	}

	@Override
	public Optional<AuthorityDescriptor> authorityFor(ObjectSpecification spec) {
		requires(spec, "spec");
		return resolverFor(spec)
				.flatMap(resolver->resolver.authorityFor(spec));
	}


	
	
}
