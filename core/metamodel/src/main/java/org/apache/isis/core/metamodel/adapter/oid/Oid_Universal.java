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

import java.io.IOException;
import java.net.URI;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.schema.common.v1.OidDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class Oid_Universal implements UniversalOid {

    private final static long serialVersionUID = 1L;
	private final static _Probe probe = _Probe.unlimited().label("Oid_Universal");
    
	private final URI universalId;
	@Getter @Setter private Version version;
	
	@Override
	public ObjectSpecId getObjectSpecId() {
		
		val path = universalId().getPath();
		
		val firstPathEntry = _Strings.splitThenStream(path, "/")
		.filter(_Strings::isNotEmpty)
		.findFirst()
		.orElse(null);
		
		return ObjectSpecId.of(firstPathEntry);
	}

	@Override
	public String getIdentifier() {
		return universalId().getQuery();
	}

	@Override
	public Bookmark asBookmark() {
		
		//TODO [2033] bad place to do this here, change API ?
		//probe.println("NOT IMPLEMENTED: 'asBookmark()'");
		
		final String objectType = getObjectSpecId().asString(); 
				//asBookmarkObjectState().getCode() + rootOid.getObjectSpecId().asString();
        final String identifier = universalId().getQuery();
        		//rootOid.getIdentifier();
        
        return new Bookmark(objectType, identifier);

	}

	@Override
	public OidDto asOidDto() {
		_Exceptions.throwNotImplemented();
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String enString() {
		if(version==null) {
			return enStringNoVersion();
		}
		return _URI.uriBuilder(universalId)
				.fragment(version.enString())
				.build()
				.toURI()
				.toString();
	}

	@Override
	public String enStringNoVersion() {
		return universalId.toString();
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public boolean isViewModel() {
		return false;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public Oid copy() {
		return of(universalId());
	}

	@Override
	public void encode(DataOutputExtended outputStream) throws IOException {
		_Exceptions.throwNotImplemented();
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public URI universalId() {
		return universalId;
	}


}
