package springapp.boot.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.webapp.IsisWebAppContextListener;

import isis.incubator.IsisBoot;
import springapp.dom.DomainModule;

@SpringBootApplication(scanBasePackageClasses= {DomainModule.class, IsisBoot.class})
public class SpringBootTomcatApplication extends SpringBootServletInitializer {

	@Bean
	public IsisWebAppContextListener isisWebAppContextListener() {

		ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
				ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;

		return new IsisWebAppContextListener();
	}


}
