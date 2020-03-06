# Spring Boot 源码环境搭建
1. 获取源码
```shell script
git clone https://github.com/spring-projects/spring-boot.git
```

2. 切换分支
```shell script
git checkout v2.2.4.RELEASE
```

3. 修改pom文件
```xml
	<properties>
		<revision>2.2.4.RELEASE</revision>
		<main.basedir>${basedir}</main.basedir>
		<disable.checks>true</disable.checks>
	</properties>

```
4. 下载maven 版本>=3.5,配置环境变量,进入spring-boot项目下执行
- 跳过检查
```shell script
mvn  clean install -DskipTests -Pfast
```
- 成功显示
```
[INFO] Spring Boot Build .................................. SUCCESS [  2.532 s]
[INFO] Spring Boot Dependencies ........................... SUCCESS [  2.683 s]
[INFO] Spring Boot Parent ................................. SUCCESS [  0.899 s]
[INFO] Spring Boot Tools .................................. SUCCESS [  0.188 s]
[INFO] Spring Boot Testing Support ........................ SUCCESS [  4.884 s]
[INFO] Spring Boot Configuration Processor ................ SUCCESS [  6.465 s]
[INFO] Spring Boot ........................................ SUCCESS [ 48.935 s]
[INFO] Spring Boot Test ................................... SUCCESS [ 11.749 s]
[INFO] Spring Boot Auto-Configure Annotation Processor .... SUCCESS [  0.685 s]
[INFO] Spring Boot AutoConfigure .......................... SUCCESS [01:03 min]
[INFO] Spring Boot Actuator ............................... SUCCESS [ 15.004 s]
[INFO] Spring Boot Actuator AutoConfigure ................. SUCCESS [ 20.976 s]
[INFO] Spring Boot Developer Tools ........................ SUCCESS [  3.896 s]
[INFO] Spring Boot Configuration Metadata ................. SUCCESS [  0.572 s]
[INFO] Spring Boot Properties Migrator .................... SUCCESS [  0.471 s]
[INFO] Spring Boot Test Auto-Configure .................... SUCCESS [  5.336 s]
[INFO] Spring Boot Loader ................................. SUCCESS [  2.009 s]
[INFO] Spring Boot Loader Tools ........................... SUCCESS [  1.388 s]
[INFO] Spring Boot Gradle Plugin .......................... SUCCESS [27:23 min]
[INFO] Spring Boot Gradle Plugin Marker Artifact .......... SUCCESS [  0.146 s]
[INFO] Spring Boot Antlib ................................. SUCCESS [  1.850 s]
[INFO] Spring Boot Configuration Docs ..................... SUCCESS [  0.489 s]
[INFO] Spring Boot Maven Plugin ........................... SUCCESS [  7.772 s]
[INFO] Spring Boot Starters ............................... SUCCESS [  1.152 s]
[INFO] Spring Boot Logging Starter ........................ SUCCESS [  1.047 s]
[INFO] Spring Boot Starter ................................ SUCCESS [  0.598 s]
[INFO] Spring Boot ActiveMQ Starter ....................... SUCCESS [  1.044 s]
[INFO] Spring Boot AMQP Starter ........................... SUCCESS [  0.353 s]
[INFO] Spring Boot AOP Starter ............................ SUCCESS [  0.336 s]
[INFO] Spring Boot Artemis Starter ........................ SUCCESS [  0.945 s]
[INFO] Spring Boot JDBC Starter ........................... SUCCESS [  0.266 s]
[INFO] Spring Boot Batch Starter .......................... SUCCESS [  0.430 s]
[INFO] Spring Boot Cache Starter .......................... SUCCESS [  0.305 s]
[INFO] Spring Boot Spring Cloud Connectors Starter ........ SUCCESS [  0.446 s]
[INFO] Spring Boot Data Cassandra Starter ................. SUCCESS [  0.661 s]
[INFO] Spring Boot Data Cassandra Reactive Starter ........ SUCCESS [  0.583 s]
[INFO] Spring Boot Data Couchbase Starter ................. SUCCESS [  0.724 s]
[INFO] Spring Boot Data Couchbase Reactive Starter ........ SUCCESS [  0.432 s]
[INFO] Spring Boot Data Elasticsearch Starter ............. SUCCESS [  1.758 s]
[INFO] Spring Boot Data JDBC Starter ...................... SUCCESS [  0.307 s]
[INFO] Spring Boot Data JPA Starter ....................... SUCCESS [  1.758 s]
[INFO] Spring Boot Data LDAP Starter ...................... SUCCESS [  0.268 s]
[INFO] Spring Boot Data MongoDB Starter ................... SUCCESS [  0.441 s]
[INFO] Spring Boot Data MongoDB Reactive Starter .......... SUCCESS [  0.364 s]
[INFO] Spring Boot Data Neo4j Starter ..................... SUCCESS [  0.632 s]
[INFO] Spring Boot Data Redis Starter ..................... SUCCESS [  0.469 s]
[INFO] Spring Boot Data Redis Reactive Starter ............ SUCCESS [  0.347 s]
[INFO] Spring Boot Json Starter ........................... SUCCESS [  0.281 s]
[INFO] Spring Boot Tomcat Starter ......................... SUCCESS [  0.265 s]
[INFO] Spring Boot Validation Starter ..................... SUCCESS [  0.348 s]
[INFO] Spring Boot Web Starter ............................ SUCCESS [  0.685 s]
[INFO] Spring Boot Data REST Starter ...................... SUCCESS [  0.761 s]
[INFO] Spring Boot Data Solr Starter ...................... SUCCESS [  1.819 s]
[INFO] Spring Boot FreeMarker Starter ..................... SUCCESS [  0.316 s]
[INFO] Spring Boot Groovy Templates Starter ............... SUCCESS [  0.631 s]
[INFO] Spring Boot HATEOAS Starter ........................ SUCCESS [  0.429 s]
[INFO] Spring Boot Integration Starter .................... SUCCESS [  0.636 s]
[INFO] Spring Boot Jersey Starter ......................... SUCCESS [  1.482 s]
[INFO] Spring Boot Jetty Starter .......................... SUCCESS [  0.645 s]
[INFO] Spring Boot JOOQ Starter ........................... SUCCESS [  0.503 s]
[INFO] Spring Boot Atomikos JTA Starter ................... SUCCESS [  0.424 s]
[INFO] Spring Boot Bitronix JTA Starter ................... SUCCESS [  0.261 s]
[INFO] Spring Boot Log4j 2 Starter ........................ SUCCESS [  0.336 s]
[INFO] Spring Boot Mail Starter ........................... SUCCESS [  0.304 s]
[INFO] Spring Boot Mustache Starter ....................... SUCCESS [  0.259 s]
[INFO] Spring Boot Actuator Starter ....................... SUCCESS [  0.328 s]
[INFO] Spring Boot OAuth2/OpenID Connect Client Starter ... SUCCESS [ 11.825 s]
[INFO] Spring Boot OAuth2 Resource Server Starter ......... SUCCESS [  0.298 s]
[INFO] Spring Boot Starter Parent ......................... SUCCESS [  0.128 s]
[INFO] Spring Boot Quartz Starter ......................... SUCCESS [  0.307 s]
[INFO] Spring Boot Reactor Netty Starter .................. SUCCESS [  0.522 s]
[INFO] Spring Boot RSocket Starter ........................ SUCCESS [  0.476 s]
[INFO] Spring Boot Security Starter ....................... SUCCESS [  0.286 s]
[INFO] Spring Boot Test Starter ........................... SUCCESS [  0.557 s]
[INFO] Spring Boot Thymeleaf Starter ...................... SUCCESS [  0.335 s]
[INFO] Spring Boot Undertow Starter ....................... SUCCESS [  0.363 s]
[INFO] Spring Boot WebFlux Starter ........................ SUCCESS [  0.472 s]
[INFO] Spring Boot WebSocket Starter ...................... SUCCESS [  0.421 s]
[INFO] Spring Boot Web Services Starter ................... SUCCESS [  0.543 s]
[INFO] Spring Boot CLI .................................... SUCCESS [  6.236 s]
[INFO] Spring Boot Docs ................................... SUCCESS [  6.520 s]
[INFO] Spring Boot Project ................................ SUCCESS [  0.015 s]
[INFO] Spring Boot Smoke Tests Invoker .................... SUCCESS [  0.315 s]
[INFO] Spring Boot Tests .................................. SUCCESS [  0.068 s]
[INFO] Spring Boot Integration Tests ...................... SUCCESS [  0.052 s]
[INFO] Spring Boot Configuration Processor Tests .......... SUCCESS [  0.456 s]
[INFO] Spring Boot DevTools Tests ......................... SUCCESS [  0.738 s]
[INFO] Spring Boot Server Tests ........................... SUCCESS [  0.579 s]
[INFO] Spring Boot Launch Script Integration Tests ........ SUCCESS [  0.549 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  31:45 min
[INFO] Finished at: 2020-03-06T14:48:15+08:00
[INFO] ------------------------------------------------------------------------


```
- 如果需要执行检查下面代码
```shell script
mvn -f spring-boot-project -Pfull clean install
```