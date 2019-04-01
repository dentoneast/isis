package isis.incubator.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface AsyncExecutionService {

	<T> CompletableFuture<T> execute(Supplier<T> supplier);
	
}
