package isis.incubator;

import org.apache.isis.core.runtime.RuntimeModule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration 
@ComponentScan(basePackageClasses={RuntimeModule.class}, includeFilters= {
		@Filter(type = FilterType.CUSTOM, classes= {BeanScanInterceptorForSpring.class})
		})
public class IsisBoot implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("!!!!!!!!!!!!!!!!!!! TODO bootstrap isis from context");
		
		
		springContext.getBeansOfType(Object.class).forEach(
		(k, v)->{
			
			if(!v.getClass().getName().startsWith("org.apache.isis") ||
					!v.getClass().getName().startsWith("domain")
					) {
				return;
			}
			
			System.out.println(String.format("key='%s', value='%s'", k, v));
		});
		
	}
	
}
