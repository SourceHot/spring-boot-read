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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.config.LocationResourceLoader.ResourceType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * {@link ConfigDataLocationResolver} for config tree locations.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @since 2.4.0
 */
public class ConfigTreeConfigDataLocationResolver implements ConfigDataLocationResolver<ConfigTreeConfigDataResource> {

	private static final String PREFIX = "configtree:";

	private final LocationResourceLoader resourceLoader;

	public ConfigTreeConfigDataLocationResolver(ResourceLoader resourceLoader) {
		this.resourceLoader = new LocationResourceLoader(resourceLoader);
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		return location.hasPrefix(PREFIX);
	}

	@Override
	public List<ConfigTreeConfigDataResource> resolve(ConfigDataLocationResolverContext context,
			ConfigDataLocation location) {
		try {
			return resolve(context, location.getNonPrefixedValue(PREFIX));
		} catch (IOException ex) {
			throw new ConfigDataLocationNotFoundException(location, ex);
		}
	}

	private List<ConfigTreeConfigDataResource> resolve(ConfigDataLocationResolverContext context, String location)
			throws IOException {
		Assert.isTrue(location.endsWith("/"),
				() -> String.format("Config tree location '%s' must end with '/'", location));
		// 判断是否匹配,不匹配直接创建ConfigTreeConfigDataResource并转换Collections返回.
		if (!this.resourceLoader.isPattern(location)) {
			return Collections.singletonList(new ConfigTreeConfigDataResource(location));
		}
		// 获取location中出现的资源对象
		Resource[] resources = this.resourceLoader.getResources(location, ResourceType.DIRECTORY);
		List<ConfigTreeConfigDataResource> resolved = new ArrayList<>(resources.length);
		for (Resource resource : resources) {
			// 获取资源对象的文件路径转换成ConfigTreeConfigDataResource
			resolved.add(new ConfigTreeConfigDataResource(resource.getFile().toPath()));
		}
		return resolved;
	}

}
