# Spring Boot 自动装配

- `org.springframework.boot.autoconfigure.SpringBootApplication`
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

	@AliasFor(annotation = EnableAutoConfiguration.class)
	Class<?>[] exclude() default {};

	@AliasFor(annotation = EnableAutoConfiguration.class)
	String[] excludeName() default {};

	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scanBasePackages() default {};

	@AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
	Class<?>[] scanBasePackageClasses() default {};

	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default true;

}

```

## EnableAutoConfiguration

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    
}
```

## AutoConfigurationImportSelector

- 类图

![image-20200320150642022](assets/image-20200320150642022.png)

### getAutoConfigurationMetadata()





```JAVA
		@Override
		public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
			Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector,
					() -> String.format("Only %s implementations are supported, got %s",
							AutoConfigurationImportSelector.class.getSimpleName(),
							deferredImportSelector.getClass().getName()));
			AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
					.getAutoConfigurationEntry(
							// 加载配置元数据
							getAutoConfigurationMetadata(), annotationMetadata);
			this.autoConfigurationEntries.add(autoConfigurationEntry);
			for (String importClassName : autoConfigurationEntry.getConfigurations()) {
				this.entries.putIfAbsent(importClassName, annotationMetadata);
			}
		}


		private AutoConfigurationMetadata getAutoConfigurationMetadata() {
			if (this.autoConfigurationMetadata == null) {
				// 加载配置信息
				this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
			}
			return this.autoConfigurationMetadata;
		}

```

- `org.springframework.boot.autoconfigure.AutoConfigurationMetadataLoader#loadMetadata(java.lang.ClassLoader)`

  ```JAVA
  	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader, String path) {
  		try {
  
  		    // 获取资源路径
  			Enumeration<URL> urls = (classLoader != null) ? classLoader.getResources(path)
  					: ClassLoader.getSystemResources(path);
  			Properties properties = new Properties();
  			while (urls.hasMoreElements()) {
  				properties.putAll(PropertiesLoaderUtils.loadProperties(new UrlResource(urls.nextElement())));
  			}
  			return loadMetadata(properties);
  		}
  		catch (IOException ex) {
  			throw new IllegalArgumentException("Unable to load @ConditionalOnClass location [" + path + "]", ex);
  		}
  	}
  
  ```

  ![image-20200320160423991](assets/image-20200320160423991.png)



- `protected static final String PATH = "META-INF/spring-autoconfigure-metadata.properties";`

  注意： 这个文件在**target**编译后的文件夹中

  相关 Issues : https://github.com/spring-projects/spring-boot/issues/11282



- 自动装配

  `spring-boot-project/spring-boot-autoconfigure/src/main/resources/META-INF/spring.factories`

  该文件内存有:

  ```
  # Auto Configure
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
  org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
  org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
  ```

  



![image-20200320162835665](assets/image-20200320162835665.png)

同样找一下redis

![image-20200320163001728](assets/image-20200320163001728.png)





- 仔细看`org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration`类



先说注解

```JAVA
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisProperties.class)
@Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })
```



### EnableConfigurationProperties

`自动映射一个POJO到Spring Boot配置文件（默认是application.properties文件）的属性集。`

- `org.springframework.boot.autoconfigure.data.redis.RedisProperties`
- 部分redis配置属性

```JAVA
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

	/**
	 * Database index used by the connection factory.
	 */
	private int database = 0;

	/**
	 * Connection URL. Overrides host, port, and password. User is ignored. Example:
	 * redis://user:password@example.com:6379
	 */
	private String url;

	/**
	 * Redis server host.
	 */
	private String host = "localhost";

	/**
	 * Login password of the redis server.
	 */
	private String password;

	/**
	 * Redis server port.
	 */
	private int port = 6379;

	/**
	 * Whether to enable SSL support.
	 */
	private boolean ssl;

	/**
	 * Connection timeout.
	 */
	private Duration timeout;

	/**
	 * Client name to be set on connections with CLIENT SETNAME.
	 */
	private String clientName;

	

}
```



- 找到一个我们用相同方式去寻找到别的一些属性处理如`org.springframework.boot.autoconfigure.jdbc.JdbcProperties` 具体展开请各位读者自行了解了







### AnnotationMetadata

回过头继续我们的主要流程

- `org.springframework.boot.autoconfigure.AutoConfigurationImportSelector.AutoConfigurationGroup#process`

  ![image-20200320163806852](assets/image-20200320163806852.png)

再此之前我们看过了`getAutoConfigurationMetadata()`的相关操作

关注 	`AnnotationMetadata annotationMetadata` 存储了一些什么

![image-20200320164145286](assets/image-20200320164145286.png)

这里简单理解

1.  mergedAnnotations 类相关的注解信息	
2.  annotationTypes 在启动类上的注解列表





### getAutoConfigurationEntry

```JAVA
	protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,
			AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		// 获取注解属性值
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
		// 获取候选配置信息
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
		// 删除重复配置
		configurations = removeDuplicates(configurations);
		// 获取 exclude 属性
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		// 校验 exclude 类
		checkExcludedClasses(configurations, exclusions);
		// 配置中删除 exclude 的属性值
		configurations.removeAll(exclusions);
		// 过滤
		configurations = filter(configurations, autoConfigurationMetadata);
		// 触发自动配置事件
		fireAutoConfigurationImportEvents(configurations, exclusions);
		// 返回
		return new AutoConfigurationEntry(configurations, exclusions);
	}

```





### getAttributes

```java
	protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
	    // name = org.springframework.boot.autoconfigure.EnableAutoConfiguration , 这是一个固定的值
		String name = getAnnotationClass().getName();
		// 获取注解的属性
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, true));
		Assert.notNull(attributes, () -> "No auto-configuration attributes found. Is " + metadata.getClassName()
				+ " annotated with " + ClassUtils.getShortName(name) + "?");
		return attributes;
	}

```



![image-20200320171138431](assets/image-20200320171138431.png)



### getCandidateConfigurations

- 读取`spring.factories`数据

```java
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
	    // 读取 org.springframework.boot.autoconfigure.EnableAutoConfiguration 相关配置
		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader());
		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}

```

![image-20200320171734270](assets/image-20200320171734270.png)

- 第一个是我自己写的一个测试用

  ```properties
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    org.sourcehot.service.HelloServiceAutoConfiguration
  ```

  





### removeDuplicates

- new 两个对象直接做数据转换，去重

```JAVA
	protected final <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

```





### getExclusions	

