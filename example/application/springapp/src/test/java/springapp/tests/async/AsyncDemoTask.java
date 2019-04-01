package springapp.tests.async;

import java.util.function.Supplier;

public class AsyncDemoTask implements Supplier<String> {

	@Override
	public String get() {
		
		System.out.println("Execute method asynchronously - " + Thread.currentThread().getName());
		
		try {
	        Thread.sleep(250);
	        return "hello world";
	    } catch (InterruptedException e) {
	        //
	    }
		return null;
	}

	
	
}
