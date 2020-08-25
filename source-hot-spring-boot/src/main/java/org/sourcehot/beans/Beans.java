package org.sourcehot.beans;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Beans {


    @Bean
    public A a() {
        return new A();
    }


    @Bean
    @ConditionalOnBean(value = A.class)
    public B b() {
        return new B();
    }
}
