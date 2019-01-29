package domainapp.modules.spring;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.springframework.context.ApplicationContext;

import domainapp.modules.spring.dom.customer.CustomerRepository;

// not to be managed by Spring
public class SpringModuleBridge {

	@Inject ApplicationContext springContext;
	
	@Produces
	public CustomerRepository getCustomerRepository() {
		return springContext.getBean(CustomerRepository.class);
	}
	
}
