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

package org.springframework.boot.devtools.autoconfigure;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.devtools.autoconfigure.DevToolsProperties.Restart;
import org.springframework.boot.devtools.classpath.ClassPathChangedEvent;
import org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher;
import org.springframework.boot.devtools.classpath.ClassPathRestartStrategy;
import org.springframework.boot.devtools.classpath.PatternClassPathRestartStrategy;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.boot.devtools.filewatch.FileSystemWatcherFactory;
import org.springframework.boot.devtools.livereload.LiveReloadServer;
import org.springframework.boot.devtools.restart.ConditionalOnInitializedRestarter;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for local development support.
 *
 * devtools 自动配置类
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Vladimir Tsanev
 * @since 1.3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnInitializedRestarter
@EnableConfigurationProperties(DevToolsProperties.class)
public class LocalDevToolsAutoConfiguration {

	/**
	 * Local LiveReload configuration.
	 * 实时重新加载配置，
	 * 注册bean
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.devtools.livereload", name = "enabled", matchIfMissing = true)
	static class LiveReloadConfiguration {

		/**
		 * 实时重启服务(应用)
		 * @param properties devtools 配置信息
		 * @return {@link LiveReloadServer}
		 */
		@Bean
		@RestartScope
		@ConditionalOnMissingBean
		LiveReloadServer liveReloadServer(DevToolsProperties properties) {
			return new LiveReloadServer(properties.getLivereload().getPort(),
					Restarter.getInstance().getThreadFactory());
		}

		@Bean
		OptionalLiveReloadServer optionalLiveReloadServer(LiveReloadServer liveReloadServer) {
			return new OptionalLiveReloadServer(liveReloadServer);
		}

		@Bean
		LiveReloadServerEventListener liveReloadServerEventListener(OptionalLiveReloadServer liveReloadServer) {
			return new LiveReloadServerEventListener(liveReloadServer);
		}

	}

	/**
	 * Local Restart Configuration.
	 *
	 * 重启配置
	 */
	@Lazy(false)
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.devtools.restart", name = "enabled", matchIfMissing = true)
	static class RestartConfiguration {

		/**
		 * devtools 的配置信息
		 */
		private final DevToolsProperties properties;

		RestartConfiguration(DevToolsProperties properties) {
			this.properties = properties;
		}

		/**
		 * 监听 变动时间
		 * @param fileSystemWatcherFactory 文件系统观察工厂
		 */
		@Bean
		ApplicationListener<ClassPathChangedEvent> restartingClassPathChangedEventListener(
				FileSystemWatcherFactory fileSystemWatcherFactory) {
			// classPath 变动事件
			return (event) -> {
				if (event.isRestartRequired()) {
					Restarter.getInstance().restart(new FileWatchingFailureHandler(fileSystemWatcherFactory));
				}
			};
		}

		/**
		 * bean {@link ClassPathFileSystemWatcher}
		 * @param fileSystemWatcherFactory
		 * @param classPathRestartStrategy
		 * @return
		 */
		@Bean
		@ConditionalOnMissingBean
		ClassPathFileSystemWatcher classPathFileSystemWatcher(FileSystemWatcherFactory fileSystemWatcherFactory,
				ClassPathRestartStrategy classPathRestartStrategy) {
			// urls = 编译后的文件存放地址
			URL[] urls = Restarter.getInstance().getInitialUrls();
			ClassPathFileSystemWatcher watcher = new ClassPathFileSystemWatcher(fileSystemWatcherFactory,
					classPathRestartStrategy, urls);
			watcher.setStopWatcherOnRestart(true);
			// 实例化后调用 org.springframework.boot.devtools.classpath.ClassPathFileSystemWatcher.afterPropertiesSet
			return watcher;
		}

		@Bean
		@ConditionalOnMissingBean
		ClassPathRestartStrategy classPathRestartStrategy() {
			return new PatternClassPathRestartStrategy(this.properties.getRestart().getAllExclude());
		}

		@Bean
		FileSystemWatcherFactory fileSystemWatcherFactory() {
			return this::newFileSystemWatcher;
		}

		@Bean
		@ConditionalOnProperty(prefix = "spring.devtools.restart", name = "log-condition-evaluation-delta",
				matchIfMissing = true)
		ConditionEvaluationDeltaLoggingListener conditionEvaluationDeltaLoggingListener() {
			return new ConditionEvaluationDeltaLoggingListener();
		}

		private FileSystemWatcher newFileSystemWatcher() {
			Restart restartProperties = this.properties.getRestart();
			FileSystemWatcher watcher = new FileSystemWatcher(true, restartProperties.getPollInterval(),
					restartProperties.getQuietPeriod());
			String triggerFile = restartProperties.getTriggerFile();
			if (StringUtils.hasLength(triggerFile)) {
				watcher.setTriggerFilter(new TriggerFileFilter(triggerFile));
			}
			List<File> additionalPaths = restartProperties.getAdditionalPaths();
			for (File path : additionalPaths) {
				watcher.addSourceFolder(path.getAbsoluteFile());
			}
			return watcher;
		}

	}

	/**
	 * 实时重载监听事件
	 */
	static class LiveReloadServerEventListener implements GenericApplicationListener {

		private final OptionalLiveReloadServer liveReloadServer;

		LiveReloadServerEventListener(OptionalLiveReloadServer liveReloadServer) {
			this.liveReloadServer = liveReloadServer;
		}

		/**
		 * 是否支持事件
		 * @param eventType 事件类型
		 * @return 是,否
		 */
		@Override
		public boolean supportsEventType(ResolvableType eventType) {
			Class<?> type = eventType.getRawClass();
			if (type == null) {
				return false;
			}
			return ContextRefreshedEvent.class.isAssignableFrom(type)
					|| ClassPathChangedEvent.class.isAssignableFrom(type);
		}

		@Override
		public boolean supportsSourceType(Class<?> sourceType) {
			return true;
		}

		/**
		 * 事件处理
		 * @param event
		 */
		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof ContextRefreshedEvent || (event instanceof ClassPathChangedEvent
					&& !((ClassPathChangedEvent) event).isRestartRequired())) {
				this.liveReloadServer.triggerReload();
			}
		}

		/**
		 * spring 的加载顺序
		 * @return
		 */
		@Override
		public int getOrder() {
			return 0;
		}

	}

}
