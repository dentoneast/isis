package springapp.tests.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.isis.core.commons.collections.Bin;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import lombok.val;
import springapp.dom.product.Product;

/**
 * There is no 'spring-data-jdo' module. So this just showcases 'native' JDO access. 
 *  
 * @since 2.0.0-M3
 */
@TestMethodOrder(OrderAnnotation.class)
class ProductTest_NativeJdo {

	final PersistenceUnitMetaData pumd = new PersistenceUnitMetaData(
			  "dynamic-unit", "RESOURCE_LOCAL", null /*rootURI*/);
	
	@BeforeEach
	void before() {
		pumd.addClassName(Product.class.getName());
		pumd.setExcludeUnlistedClasses();
		pumd.addProperty("javax.jdo.option.ConnectionDriverName", "org.h2.Driver");
		pumd.addProperty("javax.jdo.option.ConnectionURL", "jdbc:h2:mem:mypersistence");
		pumd.addProperty("javax.jdo.option.ConnectionUserName", "sa");
		pumd.addProperty("javax.jdo.option.ConnectionPassword", "");
		pumd.addProperty("datanucleus.autoCreateSchema", "true");
	}
	
	@AfterEach
	void after() {
	}
	
		
	@Test
	@Order(1)
	void persistenceManagerLoads() {
		
		PersistenceManagerFactory pmf = new JDOPersistenceManagerFactory(pumd, null /*overrideProps*/);
		PersistenceManager pm = pmf.getPersistenceManager();
		
		assertNotNull(pm);
		pm.close();
	}

	@Test
	@Order(2)
	void repositoryPersists() {
		
		PersistenceManagerFactory pmf = new JDOPersistenceManagerFactory(pumd, null /*overrideProps*/);
		Bin<Product> allProducts;
		
		{ // INSERT
			
			PersistenceManager pm = pmf.getPersistenceManager();
			
			Product product = new Product("Tablet", 80.0);
			Transaction tx = pm.currentTransaction();
			
			try {
				pm.makePersistent(product);
			} finally {
			    if (tx.isActive()) {
			        tx.rollback();
			    }
			    pm.close();
			}
		}
		
		allProducts = fetchAllProducts(pmf);
		assertTrue(allProducts.isCardinalityOne());
		assertEquals(allProducts.getSingleton().get().getName(), "Tablet");
		
		{ // READ
			
			PersistenceManager pm = pmf.getPersistenceManager();
			
			Query q = pm.newQuery(
					  "SELECT FROM " + Product.class.getName() + " WHERE price > 10");
			
			@SuppressWarnings("unchecked")
			List<Product> products = (List<Product>) q.execute();
			
			val productCount = products.size(); 
			assertEquals(1, productCount);
			
			val product = products.get(0);
			assertTrue(product.getId()>0L);
			
			pm.close();
		}
		
		{ // UPDATE
			
			PersistenceManager pm = pmf.getPersistenceManager();
			
			Query query = pm.newQuery(Product.class, "name == \"Tablet\"");
			Collection<?> result = (Collection<?>) query.execute();
			Product product = (Product) result.iterator().next();
			product.setName("Android Phone");
			
			pm.close();
		}
		
		
		allProducts = fetchAllProducts(pmf);
		assertTrue(allProducts.isCardinalityOne());
		assertEquals(allProducts.getSingleton().get().getName(), "Android Phone");
		
		{ // DELETE
			PersistenceManager pm = pmf.getPersistenceManager();
			
			Query query = pm.newQuery(Product.class, "name == \"Android Phone\"");
			Collection<?> result = (Collection<?>) query.execute();
			Product product = (Product) result.iterator().next();
			pm.deletePersistent(product);
			
			pm.close();
		}
		
		allProducts = fetchAllProducts(pmf);
		assertTrue(allProducts.isEmpty());
		
	}
	
	// -- HELPER
	
	private Bin<Product> fetchAllProducts(PersistenceManagerFactory pmf) {
		
		val pm = pmf.getPersistenceManager();
		
		try {
			@SuppressWarnings("unchecked")
			List<Product> products = (List<Product>) pm.newQuery(Product.class).execute();
			return Bin.ofCollection(products);
		} catch (Exception e) {
			// TODO: handle exception
			return Bin.empty();
		} finally {
			pm.close();
		}
		
		 
		
	}
	
}


