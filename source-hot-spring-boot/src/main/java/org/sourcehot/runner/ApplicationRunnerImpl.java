package org.sourcehot.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner, Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRunnerImpl.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		LOGGER.info("启动成功");
	}
}
