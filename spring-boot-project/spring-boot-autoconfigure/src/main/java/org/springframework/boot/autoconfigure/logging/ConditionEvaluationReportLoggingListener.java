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

package org.springframework.boot.autoconfigure.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * {@link ApplicationContextInitializer} that writes the {@link ConditionEvaluationReport}
 * to the log. Reports are logged at the {@link LogLevel#DEBUG DEBUG} level. A crash
 * report triggers an info output suggesting the user runs again with debug enabled to
 * display the report.
 * <p>
 * This initializer is not intended to be shared across multiple application context
 * instances.
 * <p>
 * 条件评估监听器
 *
 * @author Greg Turnquist
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class ConditionEvaluationReportLoggingListener
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private final Log logger = LogFactory.getLog(getClass());
	/**
	 * 日志级别
	 */
	private final LogLevel logLevelForReport;
	/**
	 * 应用上下文
	 */
	private ConfigurableApplicationContext applicationContext;
	/**
	 * 报告对象
	 */
	private ConditionEvaluationReport report;

	public ConditionEvaluationReportLoggingListener() {
		this(LogLevel.DEBUG);
	}

	public ConditionEvaluationReportLoggingListener(LogLevel logLevelForReport) {
		Assert.isTrue(isInfoOrDebug(logLevelForReport), "LogLevel must be INFO or DEBUG");
		this.logLevelForReport = logLevelForReport;
	}

	private boolean isInfoOrDebug(LogLevel logLevelForReport) {
		return LogLevel.INFO.equals(logLevelForReport) || LogLevel.DEBUG.equals(logLevelForReport);
	}

	public LogLevel getLogLevelForReport() {
		return this.logLevelForReport;
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		// 设置成员变量应用上下文
		this.applicationContext = applicationContext;
		// 添加应用监听器
		applicationContext.addApplicationListener(new ConditionEvaluationReportListener());
		// 应用上下文类型是GenericApplicationContext
		if (applicationContext instanceof GenericApplicationContext) {
			// Get the report early in case the context fails to load
			// 获取报告对象设置到成员变量中
			this.report = ConditionEvaluationReport.get(this.applicationContext.getBeanFactory());
		}
	}

	protected void onApplicationEvent(ApplicationEvent event) {
		// 获取应用上下文
		ConfigurableApplicationContext initializerApplicationContext = this.applicationContext;
		// 事件类型是ContextRefreshedEvent的处理
		if (event instanceof ContextRefreshedEvent) {
			if (((ApplicationContextEvent) event).getApplicationContext() == initializerApplicationContext) {
				// 组装条件报告结果
				logAutoConfigurationReport();
			}
		}
		// 事件类型是ApplicationFailedEvent的处理
		else if (event instanceof ApplicationFailedEvent
				&& ((ApplicationFailedEvent) event).getApplicationContext() == initializerApplicationContext) {
			// 组装条件报告结果
			logAutoConfigurationReport(true);
		}
	}

	private void logAutoConfigurationReport() {
		logAutoConfigurationReport(!this.applicationContext.isActive());
	}

	public void logAutoConfigurationReport(boolean isCrashReport) {
		if (this.report == null) {
			if (this.applicationContext == null) {
				this.logger.info("Unable to provide the conditions report due to missing ApplicationContext");
				return;
			}
			this.report = ConditionEvaluationReport.get(this.applicationContext.getBeanFactory());
		}
		if (!this.report.getConditionAndOutcomesBySource().isEmpty()) {
			if (getLogLevelForReport().equals(LogLevel.INFO)) {
				if (this.logger.isInfoEnabled()) {
					this.logger.info(new ConditionEvaluationReportMessage(this.report));
				} else if (isCrashReport) {
					logMessage("info");
				}
			} else {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(new ConditionEvaluationReportMessage(this.report));
				} else if (isCrashReport) {
					logMessage("debug");
				}
			}
		}
	}

	private void logMessage(String logLevel) {
		this.logger.info(String.format("%n%nError starting ApplicationContext. To display the "
				+ "conditions report re-run your application with '" + logLevel + "' enabled."));
	}

	private class ConditionEvaluationReportListener implements GenericApplicationListener {

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}

		@Override
		public boolean supportsEventType(ResolvableType resolvableType) {
			Class<?> type = resolvableType.getRawClass();
			if (type == null) {
				return false;
			}
			return ContextRefreshedEvent.class.isAssignableFrom(type)
					|| ApplicationFailedEvent.class.isAssignableFrom(type);
		}

		@Override
		public boolean supportsSourceType(Class<?> sourceType) {
			return true;
		}

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			ConditionEvaluationReportLoggingListener.this.onApplicationEvent(event);
		}

	}

}
