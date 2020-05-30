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

package org.springframework.boot.logging;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Common abstraction over logging systems.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Ben Hale
 * @since 1.0.0
 */
public abstract class LoggingSystem {

	/**
	 * A System property that can be used to indicate the {@link LoggingSystem} to use.
	 * 系统属性
	 */
	public static final String SYSTEM_PROPERTY = LoggingSystem.class.getName();

	/**
	 * The value of the {@link #SYSTEM_PROPERTY} that can be used to indicate that no
	 * {@link LoggingSystem} should be used.
	 */
	public static final String NONE = "none";

	/**
	 * The name used for the root logger. LoggingSystem implementations should ensure that
	 * this is the name used to represent the root logger, regardless of the underlying
	 * implementation.
	 */
	public static final String ROOT_LOGGER_NAME = "ROOT";

	/**
	 * key: 第三方日志框架的类 value: springBoot 中的处理类
	 */
	private static final Map<String, String> SYSTEMS;

	static {
		Map<String, String> systems = new LinkedHashMap<>();
		systems.put("ch.qos.logback.core.Appender", "org.springframework.boot.logging.logback.LogbackLoggingSystem");
		systems.put("org.apache.logging.log4j.core.impl.Log4jContextFactory",
				"org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
		systems.put("java.util.logging.LogManager", "org.springframework.boot.logging.java.JavaLoggingSystem");
		SYSTEMS = Collections.unmodifiableMap(systems);
	}

	/**
	 * Reset the logging system to be limit output. This method may be called before
	 * {@link #initialize(LoggingInitializationContext, String, LogFile)} to reduce
	 * logging noise until the system has been fully initialized.
	 *
	 * 初始化之前调用，目的是减少日志输出
	 *
	 */
	public abstract void beforeInitialize();

	/**
	 * Fully initialize the logging system.
	 *
	 * 初始化日志
	 *
	 * @param initializationContext the logging initialization context 日志上下文
	 * @param configLocation a log configuration location or {@code null} if default
	 * initialization is required
	 *                       配置文件地址
	 * @param logFile the log output file that should be written or {@code null} for
	 * console only output
	 */
	public void initialize(LoggingInitializationContext initializationContext, String configLocation, LogFile logFile) {
	}

	/**
	 * Clean up the logging system. The default implementation does nothing. Subclasses
	 * should override this method to perform any logging system-specific cleanup.
	 *
	 * 清除日志
	 */
	public void cleanUp() {
	}

	/**
	 * Returns a {@link Runnable} that can handle shutdown of this logging system when the
	 * JVM exits. The default implementation returns {@code null}, indicating that no
	 * shutdown is required.
	 * @return the shutdown handler, or {@code null}
	 */
	public Runnable getShutdownHandler() {
		return null;
	}

	/**
	 * Returns a set of the {@link LogLevel LogLevels} that are actually supported by the
	 * logging system.
	 *
	 * 获取支持的日志级别
	 * @return the supported levels
	 */
	public Set<LogLevel> getSupportedLogLevels() {
		return EnumSet.allOf(LogLevel.class);
	}

	/**
	 * Sets the logging level for a given logger.
	 *
	 * 设置日志级别
	 * @param loggerName the name of the logger to set ({@code null} can be used for the
	 * root logger).
	 * @param level the log level ({@code null} can be used to remove any custom level for
	 * the logger and use the default configuration instead)
	 */
	public void setLogLevel(String loggerName, LogLevel level) {
		throw new UnsupportedOperationException("Unable to set log level");
	}

	/**
	 * Returns a collection of the current configuration for all a {@link LoggingSystem}'s
	 * loggers.
	 *
	 * 获取日志配置
	 * @return the current configurations
	 * @since 1.5.0
	 */
	public List<LoggerConfiguration> getLoggerConfigurations() {
		throw new UnsupportedOperationException("Unable to get logger configurations");
	}

	/**
	 * Returns the current configuration for a {@link LoggingSystem}'s logger.
	 *
	 * 获取日志配置
	 * @param loggerName the name of the logger
	 * @return the current configuration
	 * @since 1.5.0
	 */
	public LoggerConfiguration getLoggerConfiguration(String loggerName) {
		throw new UnsupportedOperationException("Unable to get logger configuration");
	}

	/**
	 * Detect and return the logging system in use. Supports Logback and Java Logging.
	 * @param classLoader the classloader
	 * @return the logging system
	 */
	public static LoggingSystem get(ClassLoader classLoader) {
	    // 获取系统属性
		String loggingSystem = System.getProperty(SYSTEM_PROPERTY);

		if (StringUtils.hasLength(loggingSystem)) {
		    // 是不是NONE
			if (NONE.equals(loggingSystem)) {
			    // 空的日志系统
				return new NoOpLoggingSystem();
			}
			return get(classLoader, loggingSystem);
		}
		// 循环所有日志,
		return SYSTEMS.entrySet().stream().filter((entry) -> ClassUtils.isPresent(entry.getKey(), classLoader))
				.map((entry) ->
				// 实例化具体日志
				 get(classLoader, entry.getValue())).findFirst()
				.orElseThrow(() -> new IllegalStateException("No suitable logging system located"));
	}

	private static LoggingSystem get(ClassLoader classLoader, String loggingSystemClass) {
		try {
			Class<?> systemClass = ClassUtils.forName(loggingSystemClass, classLoader);
			Constructor<?> constructor = systemClass.getDeclaredConstructor(ClassLoader.class);
			constructor.setAccessible(true);
			return (LoggingSystem) constructor.newInstance(classLoader);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * {@link LoggingSystem} that does nothing.
	 * 空日志
	 */
	static class NoOpLoggingSystem extends LoggingSystem {

		@Override
		public void beforeInitialize() {

		}

		@Override
		public void setLogLevel(String loggerName, LogLevel level) {

		}

		@Override
		public List<LoggerConfiguration> getLoggerConfigurations() {
			return Collections.emptyList();
		}

		@Override
		public LoggerConfiguration getLoggerConfiguration(String loggerName) {
			return null;
		}

	}

}
