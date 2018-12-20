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
package org.apache.isis.core.unittestsupport.weld;

import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 1. Weld container is started/stopped automatically.
 * 2. By default, only the content of the test package is discovered by Weld.
 * 3. Test class is injected automatically.
 * 
 * @see <a href="https://dzone.com/articles/weld-junit-easy-testing-of-cdi-beans">quick-guide</a>
 */

@EnableWeld
class WeldHelloWorldTest {

    @Inject
    Foo foo;

    @Test
    void testFooPing() {
        Assertions.assertEquals("pong", foo.ping());
    }
    
    // -- HELPER
    
    static class Foo {

        public String ping() {
            return "pong";
        }
        
    }

}
