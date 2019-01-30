package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class _CDI_SingletonInstance<T> implements Instance<T> {

	private final T singleton;
	private Iterable<T> iterable;
	
	@Override
	public Iterator<T> iterator() {
		if(iterable==null) {
			iterable = Collections.singletonList(singleton);
		}
		return iterable.iterator();
	}

	@Override
	public T get() {
		return singleton;
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
		return false;
	}

	@Override
	public void destroy(T instance) {
		// ignore
	}

}
