package org.apache.isis.core.runtime.system.session;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

@ApplicationScoped
public class IsisSessionFactory_producer {

	@Produces @ApplicationScoped
	public IsisSessionFactory produce() {
		return isisSessionFactorySingleton.get();		
	}

	@Produces @ApplicationScoped
	public SpecificationLoader produceSpecificationLoader() {
		return isisSessionFactorySingleton.get().getSpecificationLoader();
	}
	
	// -- HELPER
	
	private final _Lazy<IsisSessionFactory> isisSessionFactorySingleton = _Lazy.of(this::newIsisSessionFactory);
	
	private final static _Probe probe = _Probe.maxCallsThenExitWithStacktrace(1).label("IsisSessionFactory_producer");
	
	private IsisSessionFactory newIsisSessionFactory() {

		probe.println("produce " + hashCode());

		final IsisSessionFactoryBuilder builder = new IsisSessionFactoryBuilder();

		// as a side-effect, if the metamodel turns out to be invalid, then
		// this will push the MetaModelInvalidException into IsisContext.
		return builder.buildSessionFactory();
	}
	
	
	
}
