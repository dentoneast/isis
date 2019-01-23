/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.runtime.system.session;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

@ApplicationScoped
public class IsisSessionProducerBean {

	@Produces @ApplicationScoped
	public IsisSessionFactory produceIsisSessionFactory() {
		return _Context.computeIfAbsent(IsisSessionFactory.class, this::newIsisSessionFactory);
	}

	@Produces @ApplicationScoped
	public SpecificationLoader produceSpecificationLoader() {
		return produceIsisSessionFactory().getSpecificationLoader();
	}
	
	@Produces @ApplicationScoped
	public AuthenticationManager produceAuthenticationManager() {
		return produceIsisSessionFactory().getAuthenticationManager();
	}
	
	@Produces @ApplicationScoped
	public AuthorizationManager produceAuthorizationManager() {
		return produceIsisSessionFactory().getAuthorizationManager();
	}
	
	@Produces @ApplicationScoped
	public PersistenceSessionFactory producePersistenceSessionFactory() {
		return produceIsisSessionFactory().getPersistenceSessionFactory();
	}
	
	
	// -- HELPER
	
	private final _Lazy<IsisSessionFactory> isisSessionFactorySingleton = 
			_Lazy.threadSafe(this::newIsisSessionFactory);
	
	private final static _Probe probe = _Probe.maxCallsThenExitWithStacktrace(10).label("IsisSessionProducerBean");
	
	private IsisSessionFactory newIsisSessionFactory() {

		try {		
		
			probe.println("newIsisSessionFactory");
	
			final IsisSessionFactoryBuilder builder = new IsisSessionFactoryBuilder();
	
			// as a side-effect, if the metamodel turns out to be invalid, then
			// this will push the MetaModelInvalidException into IsisContext.
			return builder.buildSessionFactory();
		
		} catch (Exception e) {
			System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			e.printStackTrace();
			System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			System.exit(1);
			throw e;
		}
		
	}
	
	
}
