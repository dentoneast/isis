package org.apache.isis.core.runtime.system.context.managers;

import static org.apache.isis.commons.internal.base._Strings.isEmpty;

import java.net.URI;

import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.commons.internal.uri._URI.UoidDto.UoidDtoBuilder;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @since 2.0.0-M3
 */
@RequiredArgsConstructor(staticName="of") @ToString @EqualsAndHashCode
public final class AuthorityDescriptor {
	
	@Getter private final ContainerType containerType;
	@Getter private final ContextType contextType;
	@Getter private final String contextId;
	
	public UoidDtoBuilder toUoidDtoBuilder(ObjectSpecId specId) {
		return _URI.uoidBuilder()
				.containerType(containerType)
				.contextType(contextType)
				.contextId(contextId)
				.path("/"+specId.asString()+"/")
				;
	}

	public boolean matches(URI uri) {
		if(uri==null) {
			return false;
		}
		return uri.toString().startsWith(matchingPrefix());
	}
	
	// -- HELPER
	
	private String matchingPrefix;
	
	private String matchingPrefix() {
		if(matchingPrefix==null) {
			matchingPrefix = String.format("uoid://%s@%s%s/", 
					contextType.name(),
					containerType.name(),
					isEmpty(contextId) 
						? "" : 
							":" + contextId
					);	
		}
		return matchingPrefix;
	}
	
}

