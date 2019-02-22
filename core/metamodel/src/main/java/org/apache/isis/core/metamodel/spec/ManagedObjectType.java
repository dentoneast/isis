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

package org.apache.isis.core.metamodel.spec;

import org.apache.isis.applib.services.metamodel.MetaModelService.Sort;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public enum ManagedObjectType {
	
	Other,
	DomainService,
    Value, 
    Collection,
    ViewModel,
    Mixin,
    Bean,
    Entity,
    
    ;
    
    public boolean isOther() {
        return this == Other;
    }
    
    public boolean isValue() {
        return this == Value;
    }
    
    public boolean isCollection() {
        return this == Collection;
    }
    
    public boolean isViewModel() {
        return this == ViewModel;
    }
    
    public boolean isMixin() {
        return this == Mixin;
    }

    public boolean isDomainService() {
        return this == DomainService;
    }
    
    public boolean isBean() {
        return this == Bean;
    }
    
    public boolean isEntity() {
        return this == Entity;
    }

    @Deprecated //TODO [2033] unify these 2 enums
	public Sort toSort() {
		
		if(isDomainService()) {
            return Sort.DOMAIN_SERVICE;
        }
        if(isViewModel()) {
            return Sort.VIEW_MODEL;
        }
        if(isValue()) {
            return Sort.VALUE;
        }
        if(isMixin()) {
            return Sort.MIXIN;
        }
        if(isCollection()) {
            return Sort.COLLECTION;
        }
		return Sort.UNKNOWN;
	}
	
    
}
