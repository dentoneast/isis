package springapp.dom.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "simple.CustomerMenu"
)
@DomainServiceLayout(named="Customer (Spring Demo)")
public class CustomerMenu {

	@Inject private CustomerRepository customerRepository;
	
	@Action
	@ActionLayout(cssClassFa="fa-leaf")
	public List<Customer> findByLastName(
			@ParameterLayout(named="Last Name")
			String lastName) {
		
		return customerRepository.findByLastName(lastName);
	}
	
	@Action
	@ActionLayout(cssClassFa="fa-leaf")
	public List<Customer> listAll() {
		return new ArrayList<Customer>((Collection<? extends Customer>) customerRepository.findAll());
	}
	
	@Action
	@ActionLayout(cssClassFa="fa-leaf")
	public Customer createCustomer ( 
			
			@ParameterLayout(named="First Name")
			String firstName,
			
			@ParameterLayout(named="Last Name")
			String lastName
			
			) {

		return customerRepository.save(new Customer(firstName, lastName));
	}
	
	@Action
	@ActionLayout(cssClassFa="fa-leaf")
	public List<Customer> createSomeCustomers() {

		// save a couple of customers
		customerRepository.save(new Customer("Jack", "Bauer"));
		customerRepository.save(new Customer("Chloe", "O'Brian"));
		customerRepository.save(new Customer("Kim", "Bauer"));
		customerRepository.save(new Customer("David", "Palmer"));
		customerRepository.save(new Customer("Michelle", "Dessler"));
		
		return listAll();
		
	}
	


	
}
