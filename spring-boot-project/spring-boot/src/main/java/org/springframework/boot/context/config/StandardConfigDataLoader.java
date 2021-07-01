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

import java.io.IOException;
import java.util.List;
import org.springframework.boot.context.config.ConfigData.Option;
import org.springframework.boot.context.config.ConfigData.PropertySourceOptions;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedResource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * {@link ConfigDataLoader} for {@link Resource} backed locations.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class StandardConfigDataLoader implements ConfigDataLoader<StandardConfigDataResource> {

	/**
	 * 特定配置文件
	 */
	private static final PropertySourceOptions PROFILE_SPECIFIC = PropertySourceOptions.always(Option.PROFILE_SPECIFIC);

	/**
	 * 非特定配置文件
	 */
	private static final PropertySourceOptions NON_PROFILE_SPECIFIC = PropertySourceOptions.ALWAYS_NONE;

	@Override
	public ConfigData load(ConfigDataLoaderContext context, StandardConfigDataResource resource)
			throws IOException, ConfigDataNotFoundException {
		// 判断资源是否是一个空目录,如果是将返回空结果
		if (resource.isEmptyDirectory()) {
			return ConfigData.EMPTY;
		}
		// 判断资源对象是否为空,为空抛出异常
		ConfigDataResourceNotFoundException.throwIfDoesNotExist(resource, resource.getResource());
		// 获取标准配置数据
		StandardConfigDataReference reference = resource.getReference();
		// 将标准配置数据中存储的资源地址转换成实际的资源对象
		Resource originTrackedResource = OriginTrackedResource.of(resource.getResource(),
				Origin.from(reference.getConfigDataLocation()));
		// 资源名称
		String name = String.format("Config resource '%s' via location '%s'", resource,
				reference.getConfigDataLocation());
		// 通过标准配置数据对象中的属性源加载器将资源进行加载
		List<PropertySource<?>> propertySources = reference.getPropertySourceLoader().load(name, originTrackedResource);
		// 根据资源的profile来判断PropertySourceOptions
		PropertySourceOptions options = (resource.getProfile() != null) ? PROFILE_SPECIFIC : NON_PROFILE_SPECIFIC;
		// 将读取到的资源进行转换,转换成ConfigData对象
		return new ConfigData(propertySources, options);
	}

}
