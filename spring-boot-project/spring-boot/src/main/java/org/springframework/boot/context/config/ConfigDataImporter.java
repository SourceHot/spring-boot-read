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

package org.springframework.boot.context.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.springframework.boot.logging.DeferredLogFactory;

/**
 * Imports {@link ConfigData} by {@link ConfigDataLocationResolver resolving} and
 * {@link ConfigDataLoader loading} locations. {@link ConfigDataResource resources} are
 * tracked to ensure that they are not imported multiple times.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class ConfigDataImporter {

	private final Log logger;

	/**
	 * 配置数据位置解析器集合
	 */
	private final ConfigDataLocationResolvers resolvers;

	/**
	 * 配置数据加载器集合
	 */
	private final ConfigDataLoaders loaders;

	/**
	 * 配置数据未找到行为
	 */
	private final ConfigDataNotFoundAction notFoundAction;

	/**
	 * 配置数据资源集合
	 */
	private final Set<ConfigDataResource> loaded = new HashSet<>();

	/**
	 * 配置数据位置集合
	 */
	private final Set<ConfigDataLocation> loadedLocations = new HashSet<>();

	/**
	 * 配置数据位置集合
	 */
	private final Set<ConfigDataLocation> optionalLocations = new HashSet<>();

	/**
	 * Create a new {@link ConfigDataImporter} instance.
	 *
	 * @param logFactory     the log factory
	 * @param notFoundAction the action to take when a location cannot be found
	 * @param resolvers      the config data location resolvers
	 * @param loaders        the config data loaders
	 */
	ConfigDataImporter(DeferredLogFactory logFactory, ConfigDataNotFoundAction notFoundAction,
			ConfigDataLocationResolvers resolvers, ConfigDataLoaders loaders) {
		this.logger = logFactory.getLog(getClass());
		this.resolvers = resolvers;
		this.loaders = loaders;
		this.notFoundAction = notFoundAction;
	}

	/**
	 * Resolve and load the given list of locations, filtering any that have been
	 * previously loaded.
	 *
	 * @param activationContext       the activation context
	 * @param locationResolverContext the location resolver context
	 * @param loaderContext           the loader context
	 * @param locations               the locations to resolve
	 * @return a map of the loaded locations and data
	 */
	Map<ConfigDataResolutionResult, ConfigData> resolveAndLoad(ConfigDataActivationContext activationContext,
			ConfigDataLocationResolverContext locationResolverContext, ConfigDataLoaderContext loaderContext,
			List<ConfigDataLocation> locations) {
		try {
			// 获取 profile
			Profiles profiles = (activationContext != null) ? activationContext.getProfiles() : null;
			// 解析
			List<ConfigDataResolutionResult> resolved = resolve(locationResolverContext, profiles, locations);
			// 加载
			return load(loaderContext, resolved);
		}
		catch (IOException ex) {
			throw new IllegalStateException("IO error on loading imports from " + locations, ex);
		}
	}

	/**
	 * 解析配置数据地址
	 */
	private List<ConfigDataResolutionResult> resolve(ConfigDataLocationResolverContext locationResolverContext,
			Profiles profiles, List<ConfigDataLocation> locations) {
		// 创建返回值集合
		List<ConfigDataResolutionResult> resolved = new ArrayList<>(locations.size());
		// 循环处理配置数据地址
		for (ConfigDataLocation location : locations) {
			// 进行实际解析,解析后放入返回结果集合
			resolved.addAll(resolve(locationResolverContext, profiles, location));
		}
		return Collections.unmodifiableList(resolved);
	}

	private List<ConfigDataResolutionResult> resolve(ConfigDataLocationResolverContext locationResolverContext,
			Profiles profiles, ConfigDataLocation location) {
		try {
			// 交给成员变量resolvers进行解析
			return this.resolvers.resolve(locationResolverContext, location, profiles);
		}
		catch (ConfigDataNotFoundException ex) {
			handle(ex, location, null);
			return Collections.emptyList();
		}
	}

	private Map<ConfigDataResolutionResult, ConfigData> load(ConfigDataLoaderContext loaderContext,
			List<ConfigDataResolutionResult> candidates) throws IOException {
		// 创建结果集合
		Map<ConfigDataResolutionResult, ConfigData> result = new LinkedHashMap<>();
		// 循环处理方法参数candidates
		for (int i = candidates.size() - 1; i >= 0; i--) {
			ConfigDataResolutionResult candidate = candidates.get(i);
			// 获取资源地址
			ConfigDataLocation location = candidate.getLocation();
			// 获取资源对象
			ConfigDataResource resource = candidate.getResource();
			// 判断是否是可选的资源,如果是加入到optionalLocations集合中
			if (resource.isOptional()) {
				this.optionalLocations.add(location);
			}
			// loaded中是否存在当前资源,如果存在加入到loadedLocations集合中
			if (this.loaded.contains(resource)) {
				this.loadedLocations.add(location);
			}
			else {
				try {
					// 实际加载配置数据
					ConfigData loaded = this.loaders.load(loaderContext, resource);
					// 如果数据集不为空加入到各个数据容器中
					if (loaded != null) {
						this.loaded.add(resource);
						this.loadedLocations.add(location);
						result.put(candidate, loaded);
					}
				}
				catch (ConfigDataNotFoundException ex) {
					handle(ex, location, resource);
				}
			}
		}
		return Collections.unmodifiableMap(result);
	}

	private void handle(ConfigDataNotFoundException ex, ConfigDataLocation location, ConfigDataResource resource) {
		if (ex instanceof ConfigDataResourceNotFoundException) {
			ex = ((ConfigDataResourceNotFoundException) ex).withLocation(location);
		}
		getNotFoundAction(location, resource).handle(this.logger, ex);
	}

	private ConfigDataNotFoundAction getNotFoundAction(ConfigDataLocation location, ConfigDataResource resource) {
		if (location.isOptional() || (resource != null && resource.isOptional())) {
			return ConfigDataNotFoundAction.IGNORE;
		}
		return this.notFoundAction;
	}

	Set<ConfigDataLocation> getLoadedLocations() {
		return this.loadedLocations;
	}

	Set<ConfigDataLocation> getOptionalLocations() {
		return this.optionalLocations;
	}

}
