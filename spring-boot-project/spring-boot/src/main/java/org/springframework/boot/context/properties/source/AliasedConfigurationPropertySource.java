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

package org.springframework.boot.context.properties.source;

import org.springframework.util.Assert;

/**
 * A {@link ConfigurationPropertySource} supporting name aliases.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class AliasedConfigurationPropertySource implements ConfigurationPropertySource {
	/**
	 * 配置属性源
	 */
	private final ConfigurationPropertySource source;
	/**
	 * 配置属性源名称与别名映射
	 */
	private final ConfigurationPropertyNameAliases aliases;

	AliasedConfigurationPropertySource(ConfigurationPropertySource source, ConfigurationPropertyNameAliases aliases) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(aliases, "Aliases must not be null");
		this.source = source;
		this.aliases = aliases;
	}

	@Override
	public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		// 从成员变量source中根据名称获取配置属性
		ConfigurationProperty result = getSource().getConfigurationProperty(name);
		// 配置属性为空的情况下根据别名搜索
		if (result == null) {
			ConfigurationPropertyName aliasedName = getAliases().getNameForAlias(name);
			result = getSource().getConfigurationProperty(aliasedName);
		}
		return result;
	}

	@Override
	public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		// 通过配置属性源直接获取配置属性状态
		ConfigurationPropertyState result = this.source.containsDescendantOf(name);
		// 状态不是ABSENT直接返回
		if (result != ConfigurationPropertyState.ABSENT) {
			return result;
		}
		// 别名处理
		for (ConfigurationPropertyName alias : getAliases().getAliases(name)) {
			ConfigurationPropertyState aliasResult = this.source.containsDescendantOf(alias);
			if (aliasResult != ConfigurationPropertyState.ABSENT) {
				return aliasResult;
			}
		}
		for (ConfigurationPropertyName from : getAliases()) {
			for (ConfigurationPropertyName alias : getAliases().getAliases(from)) {
				if (name.isAncestorOf(alias)) {
					if (this.source.getConfigurationProperty(from) != null) {
						return ConfigurationPropertyState.PRESENT;
					}
				}
			}
		}
		return ConfigurationPropertyState.ABSENT;
	}

	@Override
	public Object getUnderlyingSource() {
		return this.source.getUnderlyingSource();
	}

	protected ConfigurationPropertySource getSource() {
		return this.source;
	}

	protected ConfigurationPropertyNameAliases getAliases() {
		return this.aliases;
	}

}
