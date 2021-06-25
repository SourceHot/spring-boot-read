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

import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Strategy interface for creating the {@link ConfigurableApplicationContext} used by a
 * {@link SpringApplication}. Created contexts should be returned in their default form,
 * with the {@code SpringApplication} responsible for configuring and refreshing the
 * context.
 *
 * 应用上下文工厂
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @since 2.4.0
 */
@FunctionalInterface
public interface ApplicationContextFactory {

	/**
	 * A default {@link ApplicationContextFactory} implementation that will create an
	 * appropriate context for the {@link WebApplicationType}.
	 *
	 * 默认的生成应用上下文生成工厂
	 */
	ApplicationContextFactory DEFAULT = (webApplicationType) -> {
		try {
			switch (webApplicationType) {
			case SERVLET:
				return new AnnotationConfigServletWebServerApplicationContext();
			case REACTIVE:
				return new AnnotationConfigReactiveWebServerApplicationContext();
			default:
				return new AnnotationConfigApplicationContext();
			}
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable create a default ApplicationContext instance, "
					+ "you may need a custom ApplicationContextFactory", ex);
		}
	};

	/**
	 * Creates the {@link ConfigurableApplicationContext application context} for a
	 * {@link SpringApplication}, respecting the given {@code webApplicationType}.
	 * 根据类型web应用类型创建可配置的应用上下文
	 * @param webApplicationType the web application type
	 * @return the newly created application context
	 */
	ConfigurableApplicationContext create(WebApplicationType webApplicationType);

	/**
	 * Creates an {@code ApplicationContextFactory} that will create contexts by
	 * instantiating the given {@code contextClass} via its primary constructor.
	 * 根据应用上下文类型获取应用上下文工厂
	 * @param contextClass the context class
	 * @return the factory that will instantiate the context class
	 * @see BeanUtils#instantiateClass(Class)
	 */
	static ApplicationContextFactory ofContextClass(Class<? extends ConfigurableApplicationContext> contextClass) {
		return of(() -> BeanUtils.instantiateClass(contextClass));
	}

	/**
	 * Creates an {@code ApplicationContextFactory} that will create contexts by calling
	 * the given {@link Supplier}.
	 * 根据Supplier接口创建应用上下文
	 * @param supplier the context supplier, for example
	 * {@code AnnotationConfigApplicationContext::new}
	 * @return the factory that will instantiate the context class
	 */
	static ApplicationContextFactory of(Supplier<ConfigurableApplicationContext> supplier) {
		return (webApplicationType) -> supplier.get();
	}

}
