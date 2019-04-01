package springapp.tests.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.isis.commons.internal.base._Casts;

import lombok.val;

public class Futures<T> {

	private final List<CompletableFuture<T>> futures = new ArrayList<>();
	
	public Futures<T> add(CompletableFuture<T> future) {
		futures.add(future);
		return this;
	}
	
	public CompletableFuture<T> combine() {
		val combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		return _Casts.uncheckedCast(combinedFuture);
	}

	
}
