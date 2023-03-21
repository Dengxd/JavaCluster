package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@SpringBootApplication
public class MainApplication {
    static String port;
    public static void main(String[] args) throws Exception{
        SpringApplication app = new SpringApplication(MainApplication.class);
        Environment env = app.run(args).getEnvironment();
        port=env.getProperty("server.port");//获取端口号
        System.out.println(port);
    }

    @RequestMapping("/")
    @ResponseBody
    public String test(HttpServletRequest request, HttpServletResponse response){
        return port;//返回端口号
    }
}
