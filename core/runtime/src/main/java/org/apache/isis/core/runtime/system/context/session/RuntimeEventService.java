package org.apache.isis.core.runtime.system.context.session;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.core.runtime.system.session.IsisSession;

import lombok.Getter;
import lombok.Value;

/**
 * 
 * @since 2.0.0-M3
 *
 */
@Singleton
public class RuntimeEventService {

	@Inject Event<AppLifecycleEvent> appLifecycleEvent;
	@Inject Event<SessionLifecycleEvent> sessionLifecycleEvent;
	
	// -- APP
	
	public void fireAppPreMetamodel() {
		appLifecycleEvent.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreMetamodel));
	}
	
	public void fireAppPostMetamodel() {
		appLifecycleEvent.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPostMetamodel));
	}
	
	public void fireAppPreDestroy() {
		appLifecycleEvent.fire(AppLifecycleEvent.of(AppLifecycleEvent.EventType.appPreDestroy));
	}
	
	// -- SESSION
	
	public void fireSessionOpened(IsisSession session) {
		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionOpened));
	}
	
	public void fireSessionClosing(IsisSession session) {
		sessionLifecycleEvent.fire(SessionLifecycleEvent.of(session, SessionLifecycleEvent.EventType.sessionClosing));
	}
	
	// -- EVENT CLASSES
	
	@Value(staticConstructor="of")
	public static class AppLifecycleEvent {

		public static enum EventType {
			appPreMetamodel,
			appPostMetamodel,
			appPreDestroy,
		}
		
		@Getter EventType eventType;
		
	}

	
	

}
