package com.example;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
@RequestMapping("/")
@SpringBootApplication
public class MainApplication {
    public static String port;
    private static int ticketCount=1000; //电影票数量
    @Autowired
    RedissonClient redissonClient;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);

    }



    /**
     *
     * @param buyNumber 买票的数量
     * @return
     */
    @RequestMapping("/buy")
    @ResponseBody
    public String index(HttpServletRequest request, HttpSession session,int buyNumber) {
        RLock lock = redissonClient.getLock("TicketLock");//建立锁
        try {
            lock.lock();//锁住资源
            //开始卖票
            //剩余票数是否足够
            if(ticketCount>=buyNumber){
                ticketCount=ticketCount-buyNumber;
            }else{  //余票不足
                return "no ticket";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }finally {
            lock.unlock();//释放锁
        }
        return "buy success";

    }



}
