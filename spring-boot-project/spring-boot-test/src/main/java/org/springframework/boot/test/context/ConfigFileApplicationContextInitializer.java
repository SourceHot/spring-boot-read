/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.test.context;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * {@link ApplicationContextInitializer} that can be used with the
 * {@link ContextConfiguration#initializers()} to trigger loading of
 * {@literal application.properties}.
 *
 * 触发application.properties加载
 * @author Phillip Webb
 * @since 1.4.0
 * @see org.springframework.boot.context.config.ConfigFileApplicationListener
 * @deprecated since 2.4.0 for removal in 2.6.0 in favor of
 * {@link ConfigDataApplicationContextInitializer}
 */
@Deprecated
public class ConfigFileApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		new org.springframework.boot.context.config.ConfigFileApplicationListener() {
			public void apply() {
				// 添加属性源
				addPropertySources(applicationContext.getEnvironment(), applicationContext);
				// 添加处理器
				addPostProcessors(applicationContext);
			}
		}.apply();
	}

}
