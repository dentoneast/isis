package org.apache.isis.commons.internal.uri;

import static org.apache.isis.commons.internal.base._Strings.splitThenAcceptEmptyAsNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.uri._URI.UoidDto.UoidDtoBuilder;
import org.apache.isis.commons.internal.uri._URI.UriDto.UriDtoBuilder;

import lombok.Builder;
import lombok.ToString;
import lombok.val;

/**
 * @since 2.0.0-M3
 */
public final class _URI {
	
	// -- URI
	
	public static UriDtoBuilder uriBuilder() {
		return UriDto.builder();
	}
	
	public static UriDtoBuilder uriBuilder(URI template) {
		val builder = UriDto.builder()
				.scheme(template.getScheme())
				.authority(template.getAuthority())
				.path(template.getPath())
				.query(template.getQuery())
				.fragment(template.getFragment());
		return builder;
	}
	
	@Builder @ToString
	public static class UriDto {
		private final String scheme;
		private final String authority;
		private final String path;
		private final String query;
		private final String fragment;
		
		public URI toURI() {
			try {
				return new URI(scheme, authority, path, query, fragment);
			} catch (URISyntaxException e) {
				throw _Exceptions.unrecoverable(e);
			}
		}
		
	}
	
	// -- UOID
	
	private final static String UOID_SCHEME = "uoid";

	public static boolean isUoid(String string) {
		return string!=null && string.startsWith(UOID_SCHEME + "://");
	}
	
	public static UoidDtoBuilder uoidBuilder() {
		return UoidDto.builder();
	}
	
	public static UoidDtoBuilder uoidBuilder(URI template) {
		val builder = UoidDto.builder()
				.scheme(template.getScheme())
				.path(template.getPath())
				.query(template.getQuery())
				.fragment(template.getFragment());
		
		splitThenAcceptEmptyAsNull(template.getAuthority(), "@", 
				(ctxType, rhs)->{ 
					builder.contextType(ctxType!=null ? ContextType.valueOf(ctxType) : null);
					
					splitThenAcceptEmptyAsNull(rhs, ":", 
							(cont, ctxId)->{ 
								builder.containerType(cont!=null ? ContainerType.valueOf(cont) : null);
								builder.contextId(ctxId);
							});			
				});
		
		
		
		return builder;
				
	}
	
	public static enum ContainerType {
		isis,
		jdo,
		cdi,
		spring,
		;
	}
	
	public static enum ContextType {
		beans, // InversionOfControlContainer
		entities, // PersistenceContext
		;
	}
	
	@Builder @ToString
	public static class UoidDto {
		@Builder.Default private final String scheme = UOID_SCHEME;
		@Builder.Default private final ContainerType containerType = ContainerType.cdi;
		@Builder.Default private final ContextType contextType = ContextType.beans;
		private final String contextId;
		private final String path;
		private final String query;
		private final String fragment;
		
		public URI toURI() {
			try {
				return new URI(scheme, authority(), path, query, fragment);
			} catch (URISyntaxException e) {
				throw _Exceptions.unrecoverable(e);
			}
		}
		
		// -- HELPER
		
		private String authority() {
			val sb = new StringBuilder();
			if(contextType!=null) {
				sb.append(contextType.name()).append("@");
			}
			if(containerType!=null) {
				sb.append(containerType.name());
			}
			if(!_Strings.isEmpty(contextId)) {
				sb.append(":").append(contextId);
			}
			return sb.toString();
		}
		
		
	}

	//-- HELPER

	private _URI() {}

	
}
