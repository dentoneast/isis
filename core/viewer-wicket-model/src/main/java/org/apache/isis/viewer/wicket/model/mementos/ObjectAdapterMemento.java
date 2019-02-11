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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.managers.Converters;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

import lombok.val;

public interface ObjectAdapterMemento extends Serializable {

	String asString();
	Bookmark asBookmark();
	Bookmark asHintingBookmark();
	
	ObjectSpecId getObjectSpecId();
	ArrayList<ObjectAdapterMemento> getList();
	
	ObjectAdapter getObjectAdapter();
	void resetVersion();
	
	default URI toObjectUri() {
		val decoder = Converters.toUriConverter();
		return decoder.toURI(asBookmark());
	}
	
	// -- DEPRECATIONS
	
	
	@Deprecated
	default ObjectAdapter getObjectAdapter(
			ConcurrencyChecking noCheck, 
			PersistenceSession persistenceSession,
			SpecificationLoader specificationLoader) {
		return getObjectAdapter();
	}
	
	@Deprecated
	default void resetVersion(PersistenceSession persistenceSession, SpecificationLoader specificationLoader) {
		resetVersion();
	}
	
	// -- FACTORIES

	static ObjectAdapterMemento ofRootOid(RootOid rootOid) {
		return ObjectAdapterMemento_Legacy.ofRootOid(rootOid);
	}

	static ObjectAdapterMemento ofAdapter(ObjectAdapter adapter) {
		return ObjectAdapterMemento_Legacy.ofAdapter(adapter);
	}
	
	static ObjectAdapterMemento ofPojo(Object pojo) {
		return ObjectAdapterMemento_Legacy.ofPojo(pojo);
	}

	static ObjectAdapterMemento ofMementoList(Collection<ObjectAdapterMemento> modelObject, ObjectSpecId specId) {
		return ObjectAdapterMemento_Legacy.ofMementoList(modelObject, specId);
	}
	
	
	// -- 

}
