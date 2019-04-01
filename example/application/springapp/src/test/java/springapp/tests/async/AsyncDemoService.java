package springapp.tests.async;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsyncDemoService {

	@Async
	public CompletableFuture<String> asyncMethodWithReturnType() {
		
	    System.out.println("Execute method asynchronously - " + Thread.currentThread().getName());
	    
	    try {
	        Thread.sleep(250);
	        return new AsyncResult<String>("hello world !!!!")
	        		.completable();
	    } catch (InterruptedException e) {
	        //
	    }
	 
	    return null;
	}
	
	
}
