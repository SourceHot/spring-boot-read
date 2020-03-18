package org.sourcehot.runner;

public class DemoClass {
    public DemoClass() {
        System.out.println("init ");
    }

    public static void main(String[] args)throws Exception {
        Class<?> aClass = Class.forName("org.sourcehot.runner.DemoClass");
        aClass.newInstance();
    }

}
