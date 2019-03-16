package isis.incubator;

import java.io.IOException;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import isis.incubator.BeanTypeRegistry2.TypeMetaData;
import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanScanInterceptorForSpring implements TypeFilter {
	
	@Getter(lazy=true) private final BeanTypeRegistry2 typeRegistry = BeanTypeRegistry2.current();

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
		
		return getTypeRegistry().isDomainType(typeMetaData);
	}

}
