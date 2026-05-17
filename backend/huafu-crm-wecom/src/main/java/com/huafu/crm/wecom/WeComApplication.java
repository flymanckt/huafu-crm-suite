package com.huafu.crm.wecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.huafu.crm")
public class WeComApplication {
    public static void main(String[] args) { SpringApplication.run(WeComApplication.class, args); }
}
