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

package org.springframework.boot.context.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link ApplicationContextInitializer} that delegates to other initializers that are
 * specified under a {@literal context.initializer.classes} environment property.
 *
 * 委托应用上下文初始化类
 * @author Dave Syer
 * @author Phillip Webb
 * @since 1.0.0
 */
public class DelegatingApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	// NOTE: Similar to org.springframework.web.context.ContextLoader

	private static final String PROPERTY_NAME = "context.initializer.classes";

	private int order = 0;

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		// 获取环境对象
		ConfigurableEnvironment environment = context.getEnvironment();
		// 获取类
		List<Class<?>> initializerClasses = getInitializerClasses(environment);
		if (!initializerClasses.isEmpty()) {
			// 应用类
			applyInitializerClasses(context, initializerClasses);
		}
	}

	private List<Class<?>> getInitializerClasses(ConfigurableEnvironment env) {
		// 从环境对象中获取类名集合
		String classNames = env.getProperty(PROPERTY_NAME);
		// 返回结果集合
		List<Class<?>> classes = new ArrayList<>();
		// 类名集合不为空的情况下会进行拆分
		if (StringUtils.hasLength(classNames)) {
			for (String className : StringUtils.tokenizeToStringArray(classNames, ",")) {
				// 当类型是ApplicationContextInitializer的时候会加入到返回结果集合
				classes.add(getInitializerClass(className));
			}
		}
		return classes;
	}

	private Class<?> getInitializerClass(String className) throws LinkageError {
		try {
			Class<?> initializerClass = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
			Assert.isAssignable(ApplicationContextInitializer.class, initializerClass);
			return initializerClass;
		}
		catch (ClassNotFoundException ex) {
			throw new ApplicationContextException("Failed to load context initializer class [" + className + "]", ex);
		}
	}

	private void applyInitializerClasses(ConfigurableApplicationContext context, List<Class<?>> initializerClasses) {
		// 获取上下文的类型
		Class<?> contextClass = context.getClass();
		// 创建ApplicationContextInitializer集合
		List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();
		// 遍历initializerClasses将其中元素实例化加入到ApplicationContextInitializer集合
		for (Class<?> initializerClass : initializerClasses) {
			initializers.add(instantiateInitializer(contextClass, initializerClass));
		}
		// 执行ApplicationContextInitializer的方法
		applyInitializers(context, initializers);
	}

	private ApplicationContextInitializer<?> instantiateInitializer(Class<?> contextClass, Class<?> initializerClass) {
		Class<?> requireContextClass = GenericTypeResolver.resolveTypeArgument(initializerClass,
				ApplicationContextInitializer.class);
		Assert.isAssignable(requireContextClass, contextClass,
				() -> String.format(
						"Could not add context initializer [%s] as its generic parameter [%s] is not assignable "
								+ "from the type of application context used by this context loader [%s]: ",
						initializerClass.getName(), requireContextClass.getName(), contextClass.getName()));
		return (ApplicationContextInitializer<?>) BeanUtils.instantiateClass(initializerClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void applyInitializers(ConfigurableApplicationContext context,
			List<ApplicationContextInitializer<?>> initializers) {
		initializers.sort(new AnnotationAwareOrderComparator());
		for (ApplicationContextInitializer initializer : initializers) {
			initializer.initialize(context);
		}
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

}
