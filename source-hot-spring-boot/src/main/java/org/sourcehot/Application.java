package org.sourcehot;

import org.sourcehot.service.IHelloService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class Application {

	@Autowired
	Environment environment;

	@Autowired
	private IHelloService helloService;

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(Application.class);
		springApplication.run(args);
	}

	@RequestMapping("/hh")
	public Object h() {
		return helloService.hello();
	}



}
