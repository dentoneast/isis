package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class _CDI_AmbiguousInstance<T> implements Instance<T> {

	private final Iterable<T> iterable;
	
	@Override
	public Iterator<T> iterator() {
		return iterable.iterator();
	}

	@Override
	public T get() {
		throw _Exceptions.notImplemented();
	}

	@Override
	public Instance<T> select(Annotation... qualifiers) {
		throw _Exceptions.notImplemented();
	}

	@Override
	public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
		throw _Exceptions.notImplemented();
	}

	@Override
	public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		throw _Exceptions.notImplemented();
	}

	@Override
	public boolean isUnsatisfied() {
		return false;
	}

	@Override
	public boolean isAmbiguous() {
		return true;
	}

	@Override
	public void destroy(T instance) {
		// ignore
	}

}
