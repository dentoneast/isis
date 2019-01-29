package domainapp.modules.spring;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.val;

public class SpringContextBridge {
	
	@Produces @Singleton
	public ApplicationContext springContext() {
		val springContext = new SpringApplication(SpringModule.class);
		return springContext.run();
	}
	
	public void close(@Disposes ApplicationContext springContext) {
		if(springContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext)springContext).close();	
		}
	}

}
