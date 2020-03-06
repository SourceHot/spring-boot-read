package org.sourcehot.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class Application {

	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
	}

	@RequestMapping("/hh")
	public Object h() {
		return "hello ";
	}

}
