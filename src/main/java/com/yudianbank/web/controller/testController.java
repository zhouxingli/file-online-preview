package com.yudianbank.web.controller;

//import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class testController extends Thread{

    @GetMapping("/testSentinel")
   // @SentinelResource("testSentinel")
    public String testSentinel(){
        return "2021-11-01" ;
    }


    @GetMapping("/testSentinel2")
  //  @SentinelResource("testSentinel2")
    public String testSentinel2(){
        //刚开始先打印输出内容
        System.out.println("hello, My name is thread");
        try {
            //让当前线程等待2秒之后，执行
            sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //当前线程在上面等待两秒之后，打印输出
        System.out.println("hello,I am running..");
        return "testSentinel2";
    }

    @GetMapping("/hystrixTest")
//    @HystrixCommand(fallbackMethod = "findListByPlanCodesBack", commandProperties = {@HystrixProperty(name =
//            "execution" +
//                    ".isolation.thread.timeoutInMilliseconds",
//            value = "1800")})
    public String  hystrixTest(){
//刚开始先打印输出内容
        System.out.println("hello, My name is thread");
        try {
            //让当前线程等待2秒之后，执行
            sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //当前线程在上面等待两秒之后，打印输出
        System.out.println("hello,I am running..");
        return "hystrixTest";
    }


    public String findListByPlanCodesBack() throws Exception {
        System.out.println("调用数据失败...");
        throw new Exception("远程数据服务调用超时");
    }
}
