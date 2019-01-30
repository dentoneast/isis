package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.apache.isis.commons.internal.exceptions._Exceptions;

final class _CDI_EmptyInstance<T> implements Instance<T> {

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				throw new NoSuchElementException();
			}
		};
	}

	@Override
	public T get() {
		throw new NoSuchElementException();
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
		return true;
	}

	@Override
	public boolean isAmbiguous() {
		return false;
	}

	@Override
	public void destroy(T instance) {
		// ignore
	}

}
