/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

/**
 * Internal utility used to load {@link AutoConfigurationMetadata}.
 *
 * 注解配置信息加载
 *
 * @author Phillip Webb
 */
final class AutoConfigurationMetadataLoader {

	/**
	 * 地址
	 */
	protected static final String PATH = "META-INF/spring-autoconfigure-metadata.properties";

	private AutoConfigurationMetadataLoader() {
	}

	/**
	 * 加载配置信息
	 * @param classLoader
	 * @return
	 */
	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader) {
		return loadMetadata(classLoader, PATH);
	}

	/**
	 * 加载配置元信息
	 * @param classLoader 类加载器
	 * @param path 地址
	 * @return
	 */
	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader, String path) {
		try {

		    // 获取资源路径
			Enumeration<URL> urls = (classLoader != null) ? classLoader.getResources(path)
					: ClassLoader.getSystemResources(path);
			Properties properties = new Properties();
			while (urls.hasMoreElements()) {
				properties.putAll(PropertiesLoaderUtils.loadProperties(new UrlResource(urls.nextElement())));
			}
			return loadMetadata(properties);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load @ConditionalOnClass location [" + path + "]", ex);
		}
	}

	static AutoConfigurationMetadata loadMetadata(Properties properties) {
		return new PropertiesAutoConfigurationMetadata(properties);
	}

	/**
	 * {@link AutoConfigurationMetadata} implementation backed by a properties file.
	 */
	private static class PropertiesAutoConfigurationMetadata implements AutoConfigurationMetadata {

		private final Properties properties;

		PropertiesAutoConfigurationMetadata(Properties properties) {
			this.properties = properties;
		}

		@Override
		public boolean wasProcessed(String className) {
			return this.properties.containsKey(className);
		}

		@Override
		public Integer getInteger(String className, String key) {
			return getInteger(className, key, null);
		}

		@Override
		public Integer getInteger(String className, String key, Integer defaultValue) {
			String value = get(className, key);
			return (value != null) ? Integer.valueOf(value) : defaultValue;
		}

		@Override
		public Set<String> getSet(String className, String key) {
			return getSet(className, key, null);
		}

		@Override
		public Set<String> getSet(String className, String key, Set<String> defaultValue) {
			String value = get(className, key);
			return (value != null) ? StringUtils.commaDelimitedListToSet(value) : defaultValue;
		}

		@Override
		public String get(String className, String key) {
			return get(className, key, null);
		}

		@Override
		public String get(String className, String key, String defaultValue) {
			String value = this.properties.getProperty(className + "." + key);
			return (value != null) ? value : defaultValue;
		}

	}

}
