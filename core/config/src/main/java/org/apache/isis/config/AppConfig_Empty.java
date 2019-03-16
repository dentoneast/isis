package org.apache.isis.config;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.config.resource.ResourceStreamSource;

/**
 * Phasing out support for legacy AppConfig
 * 
 * @since 2.0.0-M3
 *
 */
final class AppConfig_Empty implements AppConfig {

	@Override
	public IsisConfiguration isisConfiguration() {
		return emptyIsisConfiguration;
	}
	
	// -- HELPER
	
	private final static EmptyIsisConfiguration emptyIsisConfiguration = new EmptyIsisConfiguration();
	
	private final static class EmptyIsisConfiguration implements IsisConfiguration {

		@Override
		public AppManifest getAppManifest() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream<Class<?>> streamClassesToDiscover() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IsisConfiguration createSubset(String prefix) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getBoolean(String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getBoolean(String name, boolean defaultValue) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Color getColor(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getColor(String name, Color defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Font getFont(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Font getFont(String name, Font defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getList(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getList(String name, String defaultListAsCommaSeparatedArray) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getInteger(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getInteger(String name, int defaultValue) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public IsisConfiguration getProperties(String withPrefix) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getString(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getString(String name, String defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasProperty(String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<String> asIterable() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ResourceStreamSource getResourceStreamSource() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, String> asMap() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
