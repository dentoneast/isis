package isis.incubator;

import java.io.IOException;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import org.apache.isis.config.registry.BeanTypeRegistry;
import org.apache.isis.config.registry.TypeMetaData;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanScanInterceptorForSpring implements TypeFilter {
	
	@Getter(lazy=true) private final BeanTypeRegistry typeRegistry = BeanTypeRegistry.current();

	@Override
	public boolean match(
			MetadataReader metadataReader, 
			MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		
		val classMetadata = metadataReader.getClassMetadata();
		if(!classMetadata.isConcrete()) {
			return false;
		}
		
		if(log.isInfoEnabled()) log.info("scanning concrete type {}", classMetadata.getClassName());
		
		val annotationMetadata = metadataReader.getAnnotationMetadata();
		val annotationTypes = annotationMetadata.getAnnotationTypes();
		val typeMetaData = TypeMetaData.of(classMetadata.getClassName(), annotationTypes);
		
		return getTypeRegistry().isManagedType(typeMetaData);
	}

}
