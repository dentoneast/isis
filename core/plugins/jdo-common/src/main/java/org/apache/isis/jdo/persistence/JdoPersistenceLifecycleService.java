package org.apache.isis.jdo.persistence;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.system.context.session.RuntimeEventService.AppLifecycleEvent;
import org.apache.isis.core.runtime.system.context.session.SessionLifecycleEvent;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.jdo.IsisJdoRuntimePlugin;

import lombok.val;

@Singleton
public class JdoPersistenceLifecycleService {

	private PersistenceSessionFactory persistenceSessionFactory;

	public void onAppLifecycleEvent(@Observes AppLifecycleEvent event) {

		val eventType = event.getEventType(); 

		switch (eventType) {
		case appPreMetamodel:
			create();
			break;
		case appPostMetamodel:
			init();
			break;
		case appPreDestroy:
			shutdown();
			break;

		default:
			throw _Exceptions.unmatchedCase(eventType);
		}

	}

	public void onSessionLifecycleEvent(@Observes SessionLifecycleEvent event) {

		val eventType = event.getEventType(); 

		switch (eventType) {
		case sessionOpened:
			openSession(event.getSession());
			break;
		case sessionClosing:
			closeSession();
			break;

		default:
			throw _Exceptions.unmatchedCase(eventType);
		}

	}
	
	@Produces @Singleton //XXX note: the resulting singleton is not life-cycle managed by CDI, nor are InjectionPoints resolved by CDI
	public PersistenceSessionFactory producePersistenceSessionFactory() {
		return persistenceSessionFactory;
	}

	// -- HELPER

	private void openSession(IsisSession isisSession) {
		val authenticationSession = isisSession.getAuthenticationSession();
		val persistenceSession =
				persistenceSessionFactory.createPersistenceSession(authenticationSession);
		persistenceSession.open();

		//TODO [2033] only to support IsisSessionFactoryDefault
		_Context.threadLocalPut(PersistenceSession.class, persistenceSession); 
	}

	private void closeSession() {
		val persistenceSession = _Context.threadLocalGetIfAny(PersistenceSession.class);

		if(persistenceSession != null) {
			persistenceSession.close();
		}		
	}

	private void create() {
		persistenceSessionFactory = 
				IsisJdoRuntimePlugin.get().getPersistenceSessionFactory();
		persistenceSessionFactory.init();
	}
	
	private void init() {
		//TODO [2033] specloader should rather be a CDI managed object
		val isisSessionFactory = _Context.getElseFail(IsisSessionFactory.class); 
		val specificationLoader = isisSessionFactory.getSpecificationLoader();
		_With.requires(specificationLoader, "specificationLoader");
		persistenceSessionFactory.catalogNamedQueries(specificationLoader);
	}

	private void shutdown() {
		if(persistenceSessionFactory!=null) {
			persistenceSessionFactory.shutdown();	
		}
	}

}
