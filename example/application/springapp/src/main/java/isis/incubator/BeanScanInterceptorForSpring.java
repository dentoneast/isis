package isis.incubator;

import java.io.IOException;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import lombok.val;

public class BeanScanInterceptorForSpring implements TypeFilter {

	@Override
	public boolean match(
			MetadataReader metadataReader, 
			MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		
		val classMetadata = metadataReader.getClassMetadata();
		
		if(!classMetadata.isConcrete()) {
			return false;
		}
		
		System.out.println("scanning: " + classMetadata.getClassName());

		return false;
	}

}
