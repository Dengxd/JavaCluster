package com.example;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequestMapping("/")
@EnableScheduling
@SpringBootApplication
public class MainApplication {

    @Autowired
    RedissonClient redissonClient;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);

    }



    //每天一点钟执行
    @Scheduled(cron ="0 0 1 * * ?")
    public void index() {
        RLock lock = redissonClient.getLock("MyTaskOne");//建立锁
        try {
            lock.lock();//锁住资源

            //先判断定时任务是否被集群中的其他服务器执行了，
            //其他服务器执行完定时任务，会在REDIS中加一个key-value标志
            // key=MyTaskOne:当天日期, 比如MyTaskOne:20230319
            // value=Done
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            String back=sdf.format(new Date());
            String key="MyTaskOne:"+back;
            RBucket<Object> bucket = redissonClient.getBucket(key);

            String str = (String)bucket.get();
            //如果key已经被其他服务器写入Done, 说明这个定时任务已经被别人做了
            if(str!=null && str.equals("Done")){
                return ;
            }

            //开始定时任务
            System.out.println("execute MyTaskOne");

            //定时任务执行完成
            //把Done写入redis
            bucket.set("Done",365, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            lock.unlock();//释放锁
        }


    }



}
