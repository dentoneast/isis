package domainapp.modules.spring;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import domainapp.modules.spring.dom.customer.CustomerRepository;

@ApplicationScoped // not to be managed by Spring
public class SpringModuleCDIBridge {

	@Produces @Singleton
	public CustomerRepository getCustomerRepository() {
		return SpringContextListener.currentContext().getBean(CustomerRepository.class);
	}
	
}
