package domainapp.modules.spring;

import java.util.NoSuchElementException;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.commons.internal.context._Context;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Vetoed // must not be managed by CDI
public class SpringContextListener implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
		_Context.putSingleton(ApplicationContext.class, springContext);
	}
	
	public static ApplicationContext currentContext() {
		return _Context.getOrThrow(ApplicationContext.class, 
				()-> new NoSuchElementException(
						"There is no Spring ApplicationContext stored on framework's _Context."));
	}
	
}
