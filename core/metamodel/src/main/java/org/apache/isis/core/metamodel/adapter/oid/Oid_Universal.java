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

package org.apache.isis.core.metamodel.adapter.oid;

import java.net.URI;
import java.util.ArrayList;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class Oid_Universal implements UniversalOid {

    //private final static long serialVersionUID = 1L;
	//private final static _Probe probe = _Probe.unlimited().label("Oid_Universal");
    
	@Getter(onMethod=@__({@Override})) private final URI objectUri;
	
	@Getter(lazy=true, onMethod=@__({@Override}))
	private final Version version = parseVersion();
	
	@Getter(lazy=true, onMethod=@__({@Override})) 
	private final ObjectSpecId objectSpecId = parseObjectSpecId();
	
	@Override
	public String getIdentifier() {
		return getObjectUri().getQuery();
	}
	
	@Override
	public String toString() {
		return enString();
	}
	
	// -- HELPER
	
	private ObjectSpecId parseObjectSpecId() {
		
		val path = getObjectUri().getPath();
		
		val firstPathEntry = _Strings.splitThenStream(path, "/")
		.filter(_Strings::isNotEmpty)
		.findFirst()
		.orElse(null);
		
		return ObjectSpecId.of(firstPathEntry);
	}
	
	private Version parseVersion() {
		val versionEncoded = getObjectUri().getFragment();
		if(_Strings.isEmpty(versionEncoded)) {
			return null;
		}
		
		val parts = new ArrayList<String>(3);
		_Strings.splitThenStream(versionEncoded, ":") // see org.apache.isis.core.metamodel.adapter.oid.Oid_Marshaller.SEPARATOR
		.forEach(parts::add);
		
		final String versionSequence = parts.get(0);
        final String versionUser = parts.get(1);
        final String versionUtcTimestamp = parts.get(2);
        final Version version = Version.Factory.parse(versionSequence, versionUser, versionUtcTimestamp);
		return version;
	}
	

	//@Override
	private Bookmark asBookmark() {
		
		//TODO [2033] bad place to do this here, change API ?
		//probe.println("NOT IMPLEMENTED: 'asBookmark()'");
		
		final String objectType = getObjectSpecId().asString(); 
				//asBookmarkObjectState().getCode() + rootOid.getObjectSpecId().asString();
        final String identifier = getObjectUri().getQuery();
        		//rootOid.getIdentifier();
        
        return new Bookmark(objectType, identifier);

	}

	//@Override
	private String enString() {
		val version = getVersion(); 
		if(Version.isEmpty(version)) {
			return enStringNoVersion();
		}
		val plain = _URI.uriBuilder(objectUri)
				.fragment(version.enString())
				.build()
				.toURI()
				.toString();
		
		return plain;
	}

	//@Override
	private String enStringNoVersion() {
		return objectUri.toString();
	}

	//@Override
	private boolean isTransient() {
		return false;
	}

	//@Override
	private boolean isViewModel() {
		return false;
	}

	//@Override
	private boolean isPersistent() {
		return true;
	}

	//@Override
	private UniversalOid copy() {
		return of(getObjectUri());
	}


}
