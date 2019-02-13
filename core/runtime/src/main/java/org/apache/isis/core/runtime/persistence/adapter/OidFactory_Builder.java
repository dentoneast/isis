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

package org.apache.isis.core.runtime.persistence.adapter;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject.SimpleManagedObject;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory.OidFactoryBuilder;
import org.apache.isis.core.runtime.persistence.adapter.OidFactory.OidProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

class OidFactory_Builder implements OidFactoryBuilder {
    
    private final List<OidProvider> handler = new ArrayList<>();
    private final Function<Object, ObjectSpecification> specProvider;
    
    public OidFactory_Builder(Function<Object, ObjectSpecification> specProvider) {
        this.specProvider = specProvider;
    }

    @Override
    public OidFactoryBuilder add(OidProvider oidProvider) {
        handler.add(oidProvider);
        return this;
    }

    @Override
    public OidFactory build() {
        return pojo -> {

            final ObjectSpecification spec = specProvider.apply(pojo);
            val managedObject = SimpleManagedObject.of(spec, pojo);
            
            final RootOid rootOid = handler.stream()
            .filter(h->h.isHandling(managedObject))
            .findFirst()
            .map(h->h.oidFor(managedObject))
            .orElse(null);

            requireNonNull(rootOid, () -> "Could not create an Oid for pojo: "+pojo);

            return rootOid;
        };
    }
    

}
