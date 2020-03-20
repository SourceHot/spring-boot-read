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

## org.springframework.boot.autoconfigure.AutoConfigurationImportSelector

- 类图

![image-20200320150642022](assets/image-20200320150642022.png)





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