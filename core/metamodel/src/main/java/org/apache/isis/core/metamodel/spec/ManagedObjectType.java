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

/**
 * 
 * @since 2.0.0-M3
 *
 */
enum ManagedObjectType {
	
	Other,
    Value, 
    Collection,
    ViewModel,
    Mixin,
    Bean,
    Entity,
    Wizard
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

    public boolean isBean() {
        return this == Bean;
    }
    
    public boolean isEntity() {
        return this == Entity;
    }
    
    public boolean isWizard() {
        return this == Wizard;
    }

	public static ManagedObjectType valueOf(ObjectSpecification spec) {
		
		if(spec.isParentedOrFreeCollection()) {
			return ManagedObjectType.Collection;
    	} else if(spec.isValue()) {
    		return ManagedObjectType.Value;
    	} else if(spec.isViewModel()) {
    		return ManagedObjectType.ViewModel;
    	} else if(spec.isMixin()) {
    		return ManagedObjectType.Mixin;
    	} else if(spec.isService()) {
    		return ManagedObjectType.Bean;
//    	} else if(isPersistenceCapable()) {
//    		return ManagedObjectType.Entity;
    	} else if(spec.isWizard()) {
    		return ManagedObjectType.Wizard;
    	} else {
    		return ManagedObjectType.Entity;
    	}

	}
    
}
