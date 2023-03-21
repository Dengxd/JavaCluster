package com.examples;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class PublicController {
    @RequestMapping("/login")
    public String index(HttpServletRequest request, HttpSession session) {
            return "login";
    }
    @RequestMapping("/hello")
    public String login(HttpServletRequest request, HttpSession session, String user,String password) {
        String loginUser=(String)session.getAttribute("user");
        request.setAttribute("port",MainApplication.port);
        if(loginUser!=null){
            return "hello";
        }
        if(user==null || password==null){
            request.setAttribute("message","please enter username and password!");
            return "login";
        }
        if(user.equals("abc") && password.equals("123")) {
            session.setAttribute("user",user);
            return "hello";
        }else{
            request.setAttribute("message","username or password error!");
            return "login";
        }
    }
}
