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

package org.springframework.boot.devtools.restart;

import java.lang.reflect.Method;

/**
 * Thread used to launch a restarted application. 用于启动重新启动的应用程序的线程。
 *
 * @author Phillip Webb
 */
class RestartLauncher extends Thread {

	private final String mainClassName;

	private final String[] args;

	private Throwable error;

	/**
	 * 初始化方法
	 *
	 * @param classLoader      类加载器
	 * @param mainClassName    启动类
	 * @param args             参数
	 * @param exceptionHandler 异常处理对象
	 */
	RestartLauncher(ClassLoader classLoader, String mainClassName, String[] args,
					UncaughtExceptionHandler exceptionHandler) {
		this.mainClassName = mainClassName;
		this.args = args;
		setName("restartedMain");
		setUncaughtExceptionHandler(exceptionHandler);
		setDaemon(false);
		setContextClassLoader(classLoader);
	}

	@Override
	public void run() {
		try {
			// 加载 main 方法所在的类 并且启动
			Class<?> mainClass = getContextClassLoader().loadClass(this.mainClassName);
			Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
			mainMethod.invoke(null, new Object[]{this.args});
		}
		catch (Throwable ex) {
			this.error = ex;
			getUncaughtExceptionHandler().uncaughtException(this, ex);
		}
	}

	Throwable getError() {
		return this.error;
	}

}
