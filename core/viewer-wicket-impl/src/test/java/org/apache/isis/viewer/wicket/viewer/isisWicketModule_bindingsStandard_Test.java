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

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class isisWicketModule_bindingsStandard_Test {

//FIXME    
//    @Rule
//    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);
//
//    @Mock
//    private ServletContext mockServletContext;
//
//    private IsisWicketModule isisWicketModule;
//	private Injector injector;
//	private final Class<?> from;
//	private final Class<?> to;
//
//
//	@Parameters
//	public static Collection<Object[]> params() {
//		return Arrays.asList(new Object[][] { 
//			{ ComponentFactoryRegistrar.class, ComponentFactoryRegistrarDefault.class }, 
//			{ ComponentFactoryRegistry.class, ComponentFactoryRegistryDefault.class }, 
//			{ PageClassList.class, PageClassListDefault.class },
//			{ PageClassRegistry.class, PageClassRegistryDefault.class }, 
//		});
//	}
//
//	public isisWicketModule_bindingsStandard_Test(final Class<?> from, final Class<?> to) {
//		this.from = from;
//		this.to = to;
//	}
//
//	@Before
//	public void setUp() throws Exception {
//	    
//	    _Config.clear();
//		        
//        isisWicketModule = new IsisWicketModule(mockServletContext);
//
//        context.checking(new Expectations() {{
//            allowing(mockServletContext);
//        }});
//
//		injector = Guice.createInjector(isisWicketModule, new ConfigModule());
//	}
//
//	@Test
//	public void binding() {
//		final Object instance = injector.getInstance(from);
//		assertThat(instance, is(instanceOf(to)));
//	}
//
//	// -- CONFIGURATION BINDING
//
//	private static class ConfigModule extends AbstractModule {
//
//        @Override 
//		protected void configure() {
//			bind(IsisConfiguration.class).toProvider(new Provider<IsisConfiguration>() {
//                @Override
//                public IsisConfiguration get() {
//                    return _Config.getConfiguration();
//                }
//            });
//		}
//	}

}
