package com.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MainApplication {
    public static String port;
    public static void main(String[] args) throws Exception{
        //SpringApplication.run(MainApplication.class,args);
        SpringApplication app = new SpringApplication(MainApplication.class);
        Environment env = app.run(args).getEnvironment();
        port=env.getProperty("server.port");//获取端口号
        System.out.println(port);
    }

}
