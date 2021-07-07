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

package org.springframework.boot.autoconfigure.condition;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * {@link Condition} that checks for specific resources.
 *
 * @author Dave Syer
 * @see ConditionalOnResource
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class OnResourceCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// 从注解元数据中获取ConditionalOnResource注解对应的数据表
		MultiValueMap<String, Object> attributes = metadata
				.getAllAnnotationAttributes(ConditionalOnResource.class.getName(), true);
		// 获取资源加载器
		ResourceLoader loader = context.getResourceLoader();
		// 资源地址集合
		List<String> locations = new ArrayList<>();
		// 设置资源地址集合
		collectValues(locations, attributes.get("resources"));
		Assert.isTrue(!locations.isEmpty(),
				"@ConditionalOnResource annotations must specify at least one resource location");
		// 丢失的资源地址集合
		List<String> missing = new ArrayList<>();
		for (String location : locations) {
			// 对资源地址中存在的占位符进行处理
			String resource = context.getEnvironment().resolvePlaceholders(location);
			// 通过资源加载器获取资源判断是否存在,如果不存在将加入到丢失的资源地址集合中
			if (!loader.getResource(resource).exists()) {
				missing.add(location);
			}
		}
		// 丢失的资源地址集合不为空的情况下返回匹配失败对象.
		if (!missing.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnResource.class)
					.didNotFind("resource", "resources").items(Style.QUOTE, missing));
		}
		// 返回匹配成功对象
		return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnResource.class)
				.found("location", "locations").items(locations));
	}

	private void collectValues(List<String> names, List<Object> values) {
		for (Object value : values) {
			for (Object item : (Object[]) value) {
				names.add((String) item);
			}
		}
	}

}
