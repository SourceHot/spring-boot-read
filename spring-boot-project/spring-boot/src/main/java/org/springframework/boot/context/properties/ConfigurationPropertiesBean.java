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

package org.springframework.boot.context.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Provides access to {@link ConfigurationProperties @ConfigurationProperties} bean
 * details, regardless of if the annotation was used directly or on a {@link Bean @Bean}
 * factory method. This class can be used to access {@link #getAll(ApplicationContext)
 * all} configuration properties beans in an ApplicationContext, or
 * {@link #get(ApplicationContext, Object, String) individual beans} on a case-by-case
 * basis (for example, in a {@link BeanPostProcessor}).
 *
 * @author Phillip Webb
 * @since 2.2.0
 * @see #getAll(ApplicationContext)
 * @see #get(ApplicationContext, Object, String)
 */
public final class ConfigurationPropertiesBean {

	/**
	 * 名称
	 */
	private final String name;

	/**
	 * 实例对象
	 */
	private final Object instance;

	/**
	 * ConfigurationProperties注解
	 */
	private final ConfigurationProperties annotation;


	private final Bindable<?> bindTarget;

	/**
	 * 绑定方法
	 */
	private final BindMethod bindMethod;

	private ConfigurationPropertiesBean(String name, Object instance, ConfigurationProperties annotation,
			Bindable<?> bindTarget) {
		this.name = name;
		this.instance = instance;
		this.annotation = annotation;
		this.bindTarget = bindTarget;
		this.bindMethod = BindMethod.forType(bindTarget.getType().resolve());
	}

	/**
	 * Return the name of the Spring bean.
	 * @return the bean name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the actual Spring bean instance.
	 * @return the bean instance
	 */
	public Object getInstance() {
		return this.instance;
	}

	/**
	 * Return the bean type.
	 * @return the bean type
	 */
	Class<?> getType() {
		return this.bindTarget.getType().resolve();
	}

	/**
	 * Return the property binding method that was used for the bean.
	 * @return the bind type
	 */
	public BindMethod getBindMethod() {
		return this.bindMethod;
	}

	/**
	 * Return the {@link ConfigurationProperties} annotation for the bean. The annotation
	 * may be defined on the bean itself or from the factory method that create the bean
	 * (usually a {@link Bean @Bean} method).
	 * @return the configuration properties annotation
	 */
	public ConfigurationProperties getAnnotation() {
		return this.annotation;
	}

	/**
	 * Return a {@link Bindable} instance suitable that can be used as a target for the
	 * {@link Binder}.
	 * @return a bind target for use with the {@link Binder}
	 */
	public Bindable<?> asBindTarget() {
		return this.bindTarget;
	}

	/**
	 * Return all {@link ConfigurationProperties @ConfigurationProperties} beans contained
	 * in the given application context. Both directly annotated beans, as well as beans
	 * that have {@link ConfigurationProperties @ConfigurationProperties} annotated
	 * factory methods are included.
	 * @param applicationContext the source application context
	 * @return a map of all configuration properties beans keyed by the bean name
	 */
	public static Map<String, ConfigurationPropertiesBean> getAll(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "ApplicationContext must not be null");
		if (applicationContext instanceof ConfigurableApplicationContext) {
			return getAll((ConfigurableApplicationContext) applicationContext);
		}
		Map<String, ConfigurationPropertiesBean> propertiesBeans = new LinkedHashMap<>();
		applicationContext.getBeansWithAnnotation(ConfigurationProperties.class)
				.forEach((beanName, bean) -> propertiesBeans.put(beanName, get(applicationContext, bean, beanName)));
		return propertiesBeans;
	}

	private static Map<String, ConfigurationPropertiesBean> getAll(ConfigurableApplicationContext applicationContext) {
		Map<String, ConfigurationPropertiesBean> propertiesBeans = new LinkedHashMap<>();
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		Iterator<String> beanNames = beanFactory.getBeanNamesIterator();
		while (beanNames.hasNext()) {
			String beanName = beanNames.next();
			if (isConfigurationPropertiesBean(beanFactory, beanName)) {
				try {
					Object bean = beanFactory.getBean(beanName);
					ConfigurationPropertiesBean propertiesBean = get(applicationContext, bean, beanName);
					propertiesBeans.put(beanName, propertiesBean);
				}
				catch (Exception ex) {
				}
			}
		}
		return propertiesBeans;
	}

	private static boolean isConfigurationPropertiesBean(ConfigurableListableBeanFactory beanFactory, String beanName) {
		try {
			if (beanFactory.getBeanDefinition(beanName).isAbstract()) {
				return false;
			}
			if (beanFactory.findAnnotationOnBean(beanName, ConfigurationProperties.class) != null) {
				return true;
			}
			Method factoryMethod = findFactoryMethod(beanFactory, beanName);
			return findMergedAnnotation(factoryMethod, ConfigurationProperties.class).isPresent();
		}
		catch (NoSuchBeanDefinitionException ex) {
			return false;
		}
	}

	/**
	 * Return a {@link ConfigurationPropertiesBean @ConfigurationPropertiesBean} instance
	 * for the given bean details or {@code null} if the bean is not a
	 * {@link ConfigurationProperties @ConfigurationProperties} object. Annotations are
	 * considered both on the bean itself, as well as any factory method (for example a
	 * {@link Bean @Bean} method).
	 * @param applicationContext the source application context
	 * @param bean the bean to consider
	 * @param beanName the bean name
	 * @return a configuration properties bean or {@code null} if the neither the bean or
	 * factory method are annotated with
	 * {@link ConfigurationProperties @ConfigurationProperties}
	 */
	public static ConfigurationPropertiesBean get(ApplicationContext applicationContext, Object bean, String beanName) {
		// 寻找方法
		Method factoryMethod = findFactoryMethod(applicationContext, beanName);
		// 创建ConfigurationPropertiesBean对象
		return create(beanName, bean, bean.getClass(), factoryMethod);
	}

	private static Method findFactoryMethod(ApplicationContext applicationContext, String beanName) {
		// 应用上下文是ConfigurableApplicationContext类型才处理
		if (applicationContext instanceof ConfigurableApplicationContext) {
			return findFactoryMethod((ConfigurableApplicationContext) applicationContext, beanName);
		}
		return null;
	}

	private static Method findFactoryMethod(ConfigurableApplicationContext applicationContext, String beanName) {
		return findFactoryMethod(applicationContext.getBeanFactory(), beanName);
	}

	private static Method findFactoryMethod(ConfigurableListableBeanFactory beanFactory, String beanName) {
		// 判断是否存在bean名称对应的bean定义对象
		if (beanFactory.containsBeanDefinition(beanName)) {
			// 存在搜索bean定义
			BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
			// bean定义类型是RootBeanDefinition
			if (beanDefinition instanceof RootBeanDefinition) {
				// 获取工厂方法
				Method resolvedFactoryMethod = ((RootBeanDefinition) beanDefinition).getResolvedFactoryMethod();
				if (resolvedFactoryMethod != null) {
					return resolvedFactoryMethod;
				}
			}
			// 类型不是RootBeanDefinition的情况处理
			return findFactoryMethodUsingReflection(beanFactory, beanDefinition);
		}
		return null;
	}

	private static Method findFactoryMethodUsingReflection(ConfigurableListableBeanFactory beanFactory,
			BeanDefinition beanDefinition) {
		// 获取工厂方法名称
		String factoryMethodName = beanDefinition.getFactoryMethodName();
		// 获取工厂bean名称
		String factoryBeanName = beanDefinition.getFactoryBeanName();
		// 工厂bean名称为空或者工厂方法
		if (factoryMethodName == null || factoryBeanName == null) {
			return null;
		}
		// 从容器中获取工厂bean名称对应的类型
		Class<?> factoryType = beanFactory.getType(factoryBeanName);
		// 判断是否是CGLIB代理,如果是CGLIB代理获取父类
		if (factoryType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
			factoryType = factoryType.getSuperclass();
		}
		// 创建存储容器
		AtomicReference<Method> factoryMethod = new AtomicReference<>();
		ReflectionUtils.doWithMethods(factoryType, (method) -> {
			// 方法名称相同设置
			if (method.getName().equals(factoryMethodName)) {
				factoryMethod.set(method);
			}
		});
		return factoryMethod.get();
	}

	static ConfigurationPropertiesBean forValueObject(Class<?> beanClass, String beanName) {
		ConfigurationPropertiesBean propertiesBean = create(beanName, null, beanClass, null);
		Assert.state(propertiesBean != null && propertiesBean.getBindMethod() == BindMethod.VALUE_OBJECT,
				() -> "Bean '" + beanName + "' is not a @ConfigurationProperties value object");
		return propertiesBean;
	}

	private static ConfigurationPropertiesBean create(String name, Object instance, Class<?> type, Method factory) {
		// 寻找注解
		ConfigurationProperties annotation = findAnnotation(instance, type, factory, ConfigurationProperties.class);
		// 注解为空返回空
		if (annotation == null) {
			return null;
		}
		// 寻找Validated注解
		Validated validated = findAnnotation(instance, type, factory, Validated.class);
		// 创建注解数组，包含ConfigurationProperties或者Validated注解
		Annotation[] annotations = (validated != null) ? new Annotation[] { annotation, validated }
				: new Annotation[] { annotation };
		// 得到绑定类型，
		ResolvableType bindType = (factory != null) ? ResolvableType.forMethodReturnType(factory)
				: ResolvableType.forClass(type);
		// 创建Bindable对象，其中存储的数据是类型和注解
		Bindable<Object> bindTarget = Bindable.of(bindType).withAnnotations(annotations);
		// 实例不为空的情况下需要进行数据对象的重新处理
		if (instance != null) {
			bindTarget = bindTarget.withExistingValue(instance);
		}
		// 构造函数创建对象
		return new ConfigurationPropertiesBean(name, instance, annotation, bindTarget);
	}

	private static <A extends Annotation> A findAnnotation(Object instance, Class<?> type, Method factory,
			Class<A> annotationType) {
		MergedAnnotation<A> annotation = MergedAnnotation.missing();
		if (factory != null) {
			annotation = findMergedAnnotation(factory, annotationType);
		}
		if (!annotation.isPresent()) {
			annotation = findMergedAnnotation(type, annotationType);
		}
		if (!annotation.isPresent() && AopUtils.isAopProxy(instance)) {
			annotation = MergedAnnotations.from(AopUtils.getTargetClass(instance), SearchStrategy.TYPE_HIERARCHY)
					.get(annotationType);
		}
		return annotation.isPresent() ? annotation.synthesize() : null;
	}

	private static <A extends Annotation> MergedAnnotation<A> findMergedAnnotation(AnnotatedElement element,
			Class<A> annotationType) {
		return (element != null) ? MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).get(annotationType)
				: MergedAnnotation.missing();
	}

	/**
	 * The binding method that is used for the bean.
	 * 绑定方法
	 */
	public enum BindMethod {

		/**
		 * Java Bean using getter/setter binding.
		 * 基于getset
		 */
		JAVA_BEAN,

		/**
		 * Value object using constructor binding.
		 * 基于构造函数
		 */
		VALUE_OBJECT;

		static BindMethod forType(Class<?> type) {
			return (ConfigurationPropertiesBindConstructorProvider.INSTANCE.getBindConstructor(type, false) != null)
					? VALUE_OBJECT : JAVA_BEAN;
		}

	}

}
