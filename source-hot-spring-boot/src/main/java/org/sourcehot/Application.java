package org.sourcehot;

import org.sourcehot.attr.OrgSourceHotAttr;
import org.sourcehot.service.IHelloService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
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
	ApplicationContext applicationContext;

	@Autowired
	private IHelloService helloService;

	@Autowired
	private OrgSourceHotAttr orgSourceHotAttr;

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(Application.class);
		springApplication.run(args);
	}

	@Bean
	public OrgSourceHotAttr orgSourceHotAttr() {
		return new OrgSourceHotAttr();
	}

	@RequestMapping("/hh")
	public Object h() {
      OrgSourceHotAttr bean = applicationContext.getBean(OrgSourceHotAttr.class);
      	System.out.println();
      return helloService.hello();
	}

}
