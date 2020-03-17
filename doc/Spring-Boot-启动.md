# SpringBoot 启动方法
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring-boot](https://github.com/SourceHot/spring-boot-read)
## 入口
- 通常一个简单的SpringBoot基础项目我们会有如下代码
```java
@SpringBootApplication
@RestController
@RequestMapping("/")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

```

- 值得关注的有`SpringApplication.run`以及注解`@SpringBootApplication`
### run方法
```java
	public ConfigurableApplicationContext run(String... args) {
	    // 秒表
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		// 获取监听器
		SpringApplicationRunListeners listeners = getRunListeners(args);
		// 监听器启动
		listeners.starting();
		try {
		    // application 启动参数列表
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
			// 配置忽略的bean信息
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
			// 创建应用上下文
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
		    // 准备上下文，装配bean
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
			// 上下文刷新
			refreshContext(context);
			// 刷新后做什么
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			// 监听器开始了
			listeners.started(context);
			// 唤醒
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
		    // 监听器正式运行
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}

```