package isis.incubator;

import java.io.IOException;

import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.config.registry.TypeMetaData;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanScanInterceptorForSpring implements TypeFilter {
	
	@Getter(lazy=true) private final IsisBeanTypeRegistry typeRegistry = IsisBeanTypeRegistry.current();

	@Override
	public boolean match(
			MetadataReader metadataReader, 
			MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		
		val classMetadata = metadataReader.getClassMetadata();
		if(!classMetadata.isConcrete()) {
			return false;
		}
		
		if(log.isDebugEnabled()) log.debug("scanning concrete type {}", classMetadata.getClassName());
		
		val annotationMetadata = metadataReader.getAnnotationMetadata();
		val annotationTypes = annotationMetadata.getAnnotationTypes();
		val typeMetaData = TypeMetaData.of(classMetadata.getClassName(), annotationTypes);
		
		return getTypeRegistry().isManagedType(typeMetaData);
	}

}
