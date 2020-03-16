package org.sourcehot.service;

import org.springframework.stereotype.Component;

@Component
public class IHelloServiceImpl implements IHelloService {
    public String hello() {
        return "hello";
    }
}
