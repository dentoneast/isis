package org.apache.isis.config.beans;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * Holds the set of domain services, persistent entities and fixture scripts.services
 */
public final class BeanTypeRegistry {

	public final static List<String> FRAMEWORK_PROVIDED_SERVICE_PACKAGES = _Lists.of(
			"org.apache.isis.applib",
			"org.apache.isis.core.wrapper" ,
			"org.apache.isis.core.metamodel.services" ,
			"org.apache.isis.core.runtime.services" ,
			"org.apache.isis.schema.services" ,
			"org.apache.isis.objectstore.jdo.applib.service" ,
			"org.apache.isis.viewer.restfulobjects.rendering.service" ,
			"org.apache.isis.objectstore.jdo.datanucleus.service.support" ,
			"org.apache.isis.objectstore.jdo.datanucleus.service.eventbus" ,
			"org.apache.isis.viewer.wicket.viewer.services", 
			"org.apache.isis.core.integtestsupport.components");

	public final static List<String> FRAMEWORK_PROVIDED_TYPES_FOR_SCANNING = _Lists.of(
			"org.apache.isis.config.AppConfig",
			"org.apache.isis.applib.IsisApplibModule",
			"org.apache.isis.core.wrapper.WrapperFactoryDefault",
			"org.apache.isis.core.metamodel.MetamodelModule",
			"org.apache.isis.core.runtime.RuntimeModule",
			"org.apache.isis.core.runtime.services.RuntimeServicesModule",
			"org.apache.isis.jdo.JdoModule",
			"org.apache.isis.viewer.restfulobjects.rendering.RendererContext"


			//                "org.apache.isis.objectstore.jdo.applib.service" ,
			//                "org.apache.isis.objectstore.jdo.datanucleus.service.support" ,
			//                "org.apache.isis.objectstore.jdo.datanucleus.service.eventbus" ,
			//                "org.apache.isis.viewer.wicket.viewer.services", 
			//                "org.apache.isis.core.integtestsupport.components"


			);

	private static BeanTypeRegistry instance = new BeanTypeRegistry();
	public static BeanTypeRegistry instance() {
		return instance;
	}

	// -- persistenceCapableTypes
	private Set<Class<?>> persistenceCapableTypes;
	/**
	 * @return <tt>null</tt> if no appManifest is defined
	 */
	public Set<Class<?>> getPersistenceCapableTypes() {
		return persistenceCapableTypes;
	}
	public void setPersistenceCapableTypes(final Set<Class<?>> persistenceCapableTypes) {
		this.persistenceCapableTypes = persistenceCapableTypes;
	}


	// -- mixinTypes
	private Set<Class<?>> mixinTypes;

	/**
	 * Along with {@link #getDomainServiceTypes()}, these are introspected eagerly.
	 *
	 * @return <tt>null</tt> if no appManifest is defined
	 */
	public Set<Class<?>> getMixinTypes() {
		return mixinTypes;
	}
	public void setMixinTypes(final Set<Class<?>> mixinTypes) {
		this.mixinTypes = mixinTypes;
	}


	// -- fixtureScriptTypes
	private Set<Class<? extends FixtureScript>> fixtureScriptTypes;

	/**
	 * @return <tt>null</tt> if no appManifest is defined
	 */
	public Set<Class<? extends FixtureScript>> getFixtureScriptTypes() {
		return fixtureScriptTypes;
	}
	public void setFixtureScriptTypes(final Set<Class<? extends FixtureScript>> fixtureScriptTypes) {
		this.fixtureScriptTypes = fixtureScriptTypes;
	}



	// -- domainServiceTypes
	private Set<Class<?>> domainServiceTypes;
	/**
	 * @return <tt>null</tt> if no appManifest is defined
	 */
	public Set<Class<?>> getDomainServiceTypes() {
		return domainServiceTypes;
	}
	public void setDomainServiceTypes(final Set<Class<?>> domainServiceTypes) {
		this.domainServiceTypes = domainServiceTypes;
	}


	private Set<Class<?>> domainObjectTypes;
	private Set<Class<?>> viewModelTypes;
	private Set<Class<?>> xmlElementTypes;

	public Set<Class<?>> getDomainObjectTypes() {
		return domainObjectTypes;
	}
	public void setDomainObjectTypes(final Set<Class<?>> domainObjectTypes) {
		this.domainObjectTypes = domainObjectTypes;
	}

	public Set<Class<?>> getViewModelTypes() {
		return viewModelTypes;
	}
	public void setViewModelTypes(final Set<Class<?>> viewModelTypes) {
		this.viewModelTypes = viewModelTypes;
	}

	public Set<Class<?>> getXmlElementTypes() {
		return xmlElementTypes;
	}
	public void setXmlElementTypes(final Set<Class<?>> xmlElementTypes) {
		this.xmlElementTypes = xmlElementTypes;
	}

	// -- STREAM ALL

	/**
	 * @since 2.0.0-M2
	 */
	public Stream<Class<?>> streamAllTypes() {

		return _Lists.of(persistenceCapableTypes,
				mixinTypes,
				fixtureScriptTypes,
				domainServiceTypes,
				domainObjectTypes,
				viewModelTypes,
				xmlElementTypes)
				.stream()
				.flatMap(Collection::stream)
				;
	}

}