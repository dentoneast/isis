package isis.incubator.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsyncExecutionServiceDefault implements AsyncExecutionService {

	@Transactional @Async @Override
	public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
		return new AsyncResult<T>(supplier.get()).completable();
	}

}
