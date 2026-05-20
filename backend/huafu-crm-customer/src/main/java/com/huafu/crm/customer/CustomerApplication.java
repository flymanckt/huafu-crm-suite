package com.huafu.crm.customer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.huafu.crm")
@MapperScan({"com.huafu.crm.customer.mapper", "com.huafu.crm.common.mapper"})
@EnableScheduling
public class CustomerApplication {
    public static void main(String[] args) { SpringApplication.run(CustomerApplication.class, args); }
}
