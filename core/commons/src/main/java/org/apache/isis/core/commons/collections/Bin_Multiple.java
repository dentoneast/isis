package org.apache.isis.core.commons.collections;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class Bin_Multiple<T> implements Bin<T> {
	
	private final List<T> elements;
	
	@Getter(lazy=true, onMethod=@__({@Override})) 
	private final Optional<T> first = Optional.of(elements.get(0));

	@Override
	public Cardinality getCardinality() {
		return Cardinality.MULTIPLE;
	}

	@Override
	public Stream<T> stream() {
		return elements.stream();
	}

	@Override
	public Optional<T> getSingleton() {
		return Optional.empty();
	}

	@Override
	public int size() {
		return elements.size();
	}

}
