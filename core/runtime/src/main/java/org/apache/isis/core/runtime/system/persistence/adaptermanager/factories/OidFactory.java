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

package org.apache.isis.core.runtime.system.persistence.adaptermanager.factories;

import java.util.function.Function;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.managers.Converters;
import org.apache.isis.core.runtime.system.context.managers.ManagedObjectResolver;

import lombok.val;

/**
 * @since 2.0.0-M2
 */
public interface OidFactory {
    
    RootOid oidFor(Object pojo);
    
    public interface OidProvider {
        boolean isHandling(ManagedObject managedObject);
        RootOid oidFor(ManagedObject managedObject);
    }
    
    public interface OidProvider2 {
        ManagedObjectResolver resolverFor(ManagedObject managedObject);
        default RootOid oidFor(ManagedObject managedObject, ManagedObjectResolver resolver) {
        	val objectUri = resolver.uriOf(managedObject);
        	val converter = Converters.fromUriConverter();
        	return converter.toRootOid(objectUri);
        }
		default ObjectAdapter adapterFor(ManagedObject managedObject, ManagedObjectResolver resolver) {
			return PojoAdapter.of(managedObject.getPojo(), oidFor(managedObject, resolver));
		}
    }
    
    public interface OidFactoryBuilder {
        OidFactoryBuilder add(OidProvider handler);
        OidFactory build();
    }
    
    public static OidFactoryBuilder builder(Function<Object, ObjectSpecification> specProvider) {
        return new OidFactory_Builder(specProvider);
    }

}
