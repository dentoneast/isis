package org.apache.isis.core.runtime.system.context.managers;

import static org.apache.isis.commons.internal.base._Strings.isEmpty;

import java.net.URI;

import org.apache.isis.commons.internal.uri._URI;
import org.apache.isis.commons.internal.uri._URI.ContainerType;
import org.apache.isis.commons.internal.uri._URI.ContextType;
import org.apache.isis.commons.internal.uri._URI.UoidDto.UoidDtoBuilder;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.Getter;
import lombok.Value;
import lombok.val;

/**
 * @since 2.0.0-M3
 */
@Value(staticConstructor="of")
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
		
		val prefix = String.format("uoid://%s@%s%s/", 
				containerType.name(),
				contextType.name(),
				isEmpty(contextId) 
					? "" : 
						":" + contextId
				);
		
		return uri.toString().startsWith(prefix);
	}
	
}

