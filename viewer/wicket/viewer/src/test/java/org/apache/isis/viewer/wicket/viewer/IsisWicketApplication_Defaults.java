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


package org.apache.isis.viewer.wicket.viewer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.testsupport.jmock.FixtureMockery;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.WebRequestCycleForIsis;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.protocol.http.WebRequest;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class IsisWicketApplication_Defaults {

	private FixtureMockery context = new FixtureMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	private IsisWicketApplication application;

	@Before
	public void setUp() throws Exception {
		application = new IsisWicketApplication();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void usesCustomSubclassOfAuthenticatedWebSession() {
		final Class<? extends AuthenticatedWebSession> webSessionClass = application.getWebSessionClass();
		assertThat(webSessionClass.equals(AuthenticatedWebSessionForIsis.class), is(true));
	}

	@Test
	public void providesCustomRequestCycle() {
		final WebRequest mockRequest = context.mock(WebRequest.class);
		final Response mockResponse = context.mock(Response.class);
		final RequestCycle newRequestCycle = application.newRequestCycle(mockRequest, mockResponse);
		assertThat(newRequestCycle, is(WebRequestCycleForIsis.class));
	}

	@Test
	public void providesConverterLocatorRegistersIsisSpecificConverters() {
		final IConverterLocator converterLocator = application.newConverterLocator();
		assertThat(converterLocator.getConverter(ObjectAdapter.class), is(not(nullValue())));
		assertThat(converterLocator.getConverter(ObjectAdapterMemento.class), is(not(nullValue())));
	}

}
