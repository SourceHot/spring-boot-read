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

import javax.naming.NamingException;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.util.StringUtils;

/**
 * {@link Condition} that checks for JNDI locations.
 *
 * @author Phillip Webb
 * @see ConditionalOnJndi
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnJndiCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// 从注解元数据中获取ConditionalOnJndi注解对应的数据
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(ConditionalOnJndi.class.getName()));
		// 从属性表中获取value数据值
		String[] locations = annotationAttributes.getStringArray("value");
		try {
			// 获取条件处理结果
			return getMatchOutcome(locations);
		}
		catch (NoClassDefFoundError ex) {
			return ConditionOutcome
					.noMatch(ConditionMessage.forCondition(ConditionalOnJndi.class).because("JNDI class not found"));
		}
	}

	private ConditionOutcome getMatchOutcome(String[] locations) {
		// jndi是否可用,如果不可用将返回匹配失败结果对象.
		if (!isJndiAvailable()) {
			return ConditionOutcome
					.noMatch(ConditionMessage.forCondition(ConditionalOnJndi.class).notAvailable("JNDI environment"));
		}
		// 如果locations数据量为0将返回匹配成功结果.
		if (locations.length == 0) {
			return ConditionOutcome
					.match(ConditionMessage.forCondition(ConditionalOnJndi.class).available("JNDI environment"));
		}
		// 获取jndi加载器
		JndiLocator locator = getJndiLocator(locations);
		// jndi中寻找地址
		String location = locator.lookupFirstLocation();
		// 创建描述对象
		String details = "(" + StringUtils.arrayToCommaDelimitedString(locations) + ")";
		// jndi中的地址搜索对象不为空返回匹配成功结果
		if (location != null) {
			return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnJndi.class, details)
					.foundExactly("\"" + location + "\""));
		}
		// jndi中的地址搜索对象为空返回匹配失败结果对象
		return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnJndi.class, details)
				.didNotFind("any matching JNDI location").atAll());
	}

	protected boolean isJndiAvailable() {
		return JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable();
	}

	protected JndiLocator getJndiLocator(String[] locations) {
		return new JndiLocator(locations);
	}

	protected static class JndiLocator extends JndiLocatorSupport {

		private String[] locations;

		public JndiLocator(String[] locations) {
			this.locations = locations;
		}

		public String lookupFirstLocation() {
			for (String location : this.locations) {
				try {
					lookup(location);
					return location;
				}
				catch (NamingException ex) {
					// Swallow and continue
				}
			}
			return null;
		}

	}

}
