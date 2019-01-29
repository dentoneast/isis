package domainapp.modules.spring.dom.customer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "simple.CustomerMenu"
)
@DomainObjectLayout(named="Customer (Spring Demo)")
@Singleton
public class CustomerMenu {

	@Inject private CustomerRepository customerRepository;
	
	@Action
	@ActionLayout(cssClassFa="fa-leaf")
	public List<Customer> findByLastName(String lastName) {
		return customerRepository.findByLastName(lastName);
	}
	
}
