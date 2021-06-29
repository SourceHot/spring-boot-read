/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Utility class for converting one type of {@link Environment} to another.
 * <p>
 * 环境转换类
 *
 * @author Ethan Rubinson
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
final class EnvironmentConverter {

	/**
	 * ConfigurableWebEnvironment的类全路径
	 */
	private static final String CONFIGURABLE_WEB_ENVIRONMENT_CLASS = "org.springframework.web.context.ConfigurableWebEnvironment";

	/**
	 * servlet环境配置名称
	 */
	private static final Set<String> SERVLET_ENVIRONMENT_SOURCE_NAMES;

	static {
		Set<String> names = new HashSet<>();
		// servletContextInitParams
		names.add(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME);
		// servletConfigInitParams
		names.add(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME);
		// jndiProperties
		names.add(StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME);
		SERVLET_ENVIRONMENT_SOURCE_NAMES = Collections.unmodifiableSet(names);
	}

	/**
	 * 类加载器
	 */
	private final ClassLoader classLoader;

	/**
	 * Creates a new {@link EnvironmentConverter} that will use the given
	 * {@code classLoader} during conversion.
	 *
	 * @param classLoader the class loader to use
	 */
	EnvironmentConverter(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Converts the given {@code environment} to the given {@link StandardEnvironment}
	 * type. If the environment is already of the same type, no conversion is performed
	 * and it is returned unchanged.
	 *
	 * @param environment the Environment to convert
	 * @param type        the type to convert the Environment to
	 * @return the converted Environment
	 */
	StandardEnvironment convertEnvironmentIfNecessary(ConfigurableEnvironment environment,
			Class<? extends StandardEnvironment> type) {
		// 类型相同的情况下进行强制类型转换
		if (type.equals(environment.getClass())) {
			return (StandardEnvironment) environment;
		}
		// 进一步转换
		return convertEnvironment(environment, type);
	}

	private StandardEnvironment convertEnvironment(ConfigurableEnvironment environment,
			Class<? extends StandardEnvironment> type) {
		// 通过type创建StandardEnvironment类型的对象
		StandardEnvironment result = createEnvironment(type);
		// 设置激活的profile
		result.setActiveProfiles(environment.getActiveProfiles());
		// 设置转换服务
		result.setConversionService(environment.getConversionService());
		// 属性拷贝
		copyPropertySources(environment, result);
		// 返回结果
		return result;
	}

	private StandardEnvironment createEnvironment(Class<? extends StandardEnvironment> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			return new StandardEnvironment();
		}
	}

	private void copyPropertySources(ConfigurableEnvironment source, StandardEnvironment target) {
		removePropertySources(target.getPropertySources(), isServletEnvironment(target.getClass(), this.classLoader));
		for (PropertySource<?> propertySource : source.getPropertySources()) {
			if (!SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(propertySource.getName())) {
				target.getPropertySources().addLast(propertySource);
			}
		}
	}

	private boolean isServletEnvironment(Class<?> conversionType, ClassLoader classLoader) {
		try {
			Class<?> webEnvironmentClass = ClassUtils.forName(CONFIGURABLE_WEB_ENVIRONMENT_CLASS, classLoader);
			return webEnvironmentClass.isAssignableFrom(conversionType);
		} catch (Throwable ex) {
			return false;
		}
	}

	private void removePropertySources(MutablePropertySources propertySources, boolean isServletEnvironment) {
		Set<String> names = new HashSet<>();
		for (PropertySource<?> propertySource : propertySources) {
			names.add(propertySource.getName());
		}
		for (String name : names) {
			if (!isServletEnvironment || !SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(name)) {
				propertySources.remove(name);
			}
		}
	}

}
