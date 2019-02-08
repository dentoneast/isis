package org.apache.isis.core.runtime.system.persistence;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.adapter.oid.UniversalOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.contextmanger.ContextManager;
import org.apache.isis.core.runtime.system.context.IsisContext;

import lombok.Getter;
import lombok.Value;
import lombok.val;

/**
 * Note: don't use ObjectAdapter
 * 
 * @since 2.0.0-M3
 */
public interface UniversalObjectManager {

	static UniversalObjectManager current() {
		return UniversalObjectManagerSample.INSTANCE;
	}

	Stream<ResolveResult> resolve(Stream<UniversalOid> uoids);

	// -- RESULT CONTAINER 
	
	@Value(staticConstructor="of")
	static final class ResolveResult {
		@Getter private final UniversalOid uoid; 
		@Getter private final ManagedObject managedObject;
	}
	
	// -- SAMPLE
	
	static class UniversalObjectManagerSample implements UniversalObjectManager {

		static UniversalObjectManagerSample INSTANCE = new UniversalObjectManagerSample();
		
		final _Lazy<ContextManager> contextManager = _Lazy.of(()->
			IsisContext.getServiceRegistry().lookupServiceElseFail(ContextManager.class));

		@Override
		public Stream<ResolveResult> resolve(Stream<UniversalOid> uoids) {
			val contextManager = this.contextManager.get();
			
			return stream(uoids)
					.flatMap(uoid->{
						val instance = contextManager.resolve(uoid);
						return instance.stream()
						.map(managedObject->ResolveResult.of(uoid, managedObject));
					})
					;
		}
		
	}

}
