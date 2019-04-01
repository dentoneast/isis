package springapp.tests.persistence;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.val;
import springapp.boot.test.SpringBootTestApplication;
import springapp.dom.customer.Customer;
import springapp.dom.customer.CustomerRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootTestApplication.class)
@AutoConfigureTestDatabase
@TestMethodOrder(OrderAnnotation.class)
class CustomerTest {

	@Autowired
	private CustomerRepository customerRepository;

	@BeforeEach
	void before() {
	}
	
	@AfterEach
	void after() {
	}
	
	@Test
	@Order(1)
	void contextLoads() {
		assertNotNull(customerRepository);
	}

	@Test
	@Order(2)
	void repositoryPersists() {
		
		assertNotNull(customerRepository);
		
		{ // INSERT
			
			customerRepository.save(new Customer("Jack", "Bauer"));
			
			val bauers = customerRepository.findByLastName("Bauer");
			val bauersCounted = bauers.size(); 
			assertEquals(1, bauersCounted);
			
			val bauer = bauers.get(0);
			assertTrue(bauer.getId()>0L);
		}
		
		{ // DELETE
			val bauers = customerRepository.findByLastName("Bauer");
			val bauer = bauers.get(0);
			customerRepository.delete(bauer);
			
			val bauersCounted = customerRepository.findByLastName("Bauer").size();
			assertEquals(0, bauersCounted);
		}
		
	}
	
}


