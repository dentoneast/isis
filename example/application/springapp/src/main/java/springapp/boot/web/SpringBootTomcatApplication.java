package springapp.boot.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import isis.incubator.IsisBoot;
import springapp.dom.DomainModule;

@SpringBootApplication(scanBasePackageClasses= {DomainModule.class, IsisBoot.class})
public class SpringBootTomcatApplication extends SpringBootServletInitializer {

//	@Bean
//	public IsisWebAppContextListener isisWebAppContextListener() {
//		return new IsisWebAppContextListener();
//	}


}
