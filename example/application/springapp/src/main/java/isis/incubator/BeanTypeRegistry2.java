package isis.incubator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.Value;
import lombok.val;

public class BeanTypeRegistry2 implements AutoCloseable {

	public static BeanTypeRegistry2 current() {
		return _Context.computeIfAbsent(BeanTypeRegistry2.class, BeanTypeRegistry2::new);
	}
	
	@Getter private final Set<Class<?>> entityTypes = new HashSet<>();
	@Getter private final Set<Class<?>> mixinTypes = new HashSet<>();
	@Getter private final Set<Class<?>> fixtureScriptTypes = new HashSet<>();
	@Getter private final Set<Class<?>> domainServiceTypes = new HashSet<>();
	@Getter private final Set<Class<?>> domainObjectTypes = new HashSet<>();
	@Getter private final Set<Class<?>> viewModelTypes = new HashSet<>();
	@Getter private final Set<Class<?>> xmlElementTypes = new HashSet<>();

	private final List<Set<Class<?>>> allTypeSets = _Lists.of(
			entityTypes,
			mixinTypes,
			fixtureScriptTypes,
			domainServiceTypes,
			domainObjectTypes,
			viewModelTypes,
			xmlElementTypes);
	
	
	@Override
	public void close() {
		allTypeSets.forEach(Set::clear);
	}

	//FIXME[2033] don't categorize this early, instead push candidate classes onto a queue for 
	// later processing when the SpecLoader initializes.
	public boolean isDomainType(TypeMetaData typeMetaData) {
		boolean isToBeIocManaged = false;
		
		if(typeMetaData.hasDomainServiceAnnotation()) {
			domainServiceTypes.add(typeMetaData.getUnderlyingClass());
		}
		
		if(typeMetaData.hasDomainObjectAnnotation()) {
			domainObjectTypes.add(typeMetaData.getUnderlyingClass());
		}
		
		if(typeMetaData.hasViewModelAnnotation()) {
			domainObjectTypes.add(typeMetaData.getUnderlyingClass());
		}
		
		if(typeMetaData.hasSingletonAnnotation()) {
			isToBeIocManaged = true;
		}
		
		return isToBeIocManaged;
	}
	
	// -- TYPE META DATA
	
	@Value(staticConstructor="of")
	public static class TypeMetaData {
		
		/**
		 * Fully qualified name of the underlying class.
		 */
		String className;
		
		/**
		 * Fully qualified class names of all annotation types that are present on the underlying class.
		 */
		Set<String> annotationTypes;
		
		public boolean hasSingletonAnnotation() {
			return annotationTypes.contains(singletonAnnotation);
		}
		
		public boolean hasDomainServiceAnnotation() {
			return annotationTypes.contains(domainServiceAnnotation);
		}
		
		public boolean hasDomainObjectAnnotation() {
			return annotationTypes.contains(domainObjectAnnotation);
		}
		
		public boolean hasMixinAnnotation() {
			return annotationTypes.contains(mixinAnnotation);
		}
		
		public boolean hasViewModelAnnotation() {
			return annotationTypes.contains(viewModelAnnotation);
		}
		
		/**
		 * @return the underlying class of this TypeMetaData
		 */
		public Class<?> getUnderlyingClass() {
			try {
				return _Context.loadClass(className);
			} catch (ClassNotFoundException e) {
				val msg = String.format("Failed to load class for name '%s'", className);
				throw _Exceptions.unrecoverable(msg, e);
			}
		}
		
		private final static String singletonAnnotation = javax.inject.Singleton.class.getName(); 
		private final static String domainServiceAnnotation = 
				org.apache.isis.applib.annotation.DomainService.class.getName();
		private final static String domainObjectAnnotation = 
				org.apache.isis.applib.annotation.DomainObject.class.getName();
		private final static String mixinAnnotation = 
				org.apache.isis.applib.annotation.Mixin.class.getName();
		private final static String viewModelAnnotation = 
				org.apache.isis.applib.annotation.ViewModel.class.getName();

		
	}
	
}
