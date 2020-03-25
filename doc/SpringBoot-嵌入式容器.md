# SpringBoot 嵌入式容器
- Author: [HuiFer](https://github.com/huifer)
- 源码阅读仓库: [SourceHot-spring-boot](https://github.com/SourceHot/spring-boot-read)





## 依赖

- 一个web项目我们最基础的依赖就是**`spring-boot-starter-web`**

![image-20200325085209824](assets/image-20200325085209824.png)





## 配置

- 最直观的一个配置`server.port=8080` tomcat 中我们也可以配置服务端口，对此我怀疑保存`server.port`属性的对象中应该有何tomcat相关的东西

  - `org.springframework.boot.autoconfigure.web.ServerProperties`

  - ![image-20200325085708416](assets/image-20200325085708416.png)

    果不其然李曼存放了 `tomcat`,`servlet`,`jetty` ,`netty `这些容器的对象

- 补充： 替换tomcat

```XML

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
 
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```





- 已经找到关键类了，剩下的就去看那些地方用了这个类，开始源码分析。



- 使用的类，我们这里关注的是`TomcatWebServerFactoryCustomizer` , 这个类名和服务相关因此我已这个作为主要的观察

![image-20200325090451465](assets/image-20200325090451465.png)





- 看`TomcatWebServerFactoryCustomizer`类 找一找调用的地方

  ![image-20200325090738203](assets/image-20200325090738203.png)

  - 重大发现 `EmbeddedWebServerFactoryCustomizerAutoConfiguration` 

    - 第一个单词`Embedded`嵌入式

    - 整个类的意思：嵌入式Web服务器出厂自定义程序自动配置

    - 重点不是说翻译这个单词，这只是作为我阅读的一个依据以及寻找的一个方式。

    - 这个类有

      ![image-20200325090946470](assets/image-20200325090946470.png)

      - 几个web服务容器都在了可以正式开始了，这个就是**入口**







## 源码

- 断点打上开始了

![image-20200325091434110](assets/image-20200325091434110.png)



配置信息：

```YAML
server:
  port: 9999
  tomcat:
    # 最大线程数
    max-threads: 6
    # 最小线程数
    min-spare-threads: 3
    # 队列长度
    accept-count: 10
    # 最大链接数
    max-connections: 1000
```

