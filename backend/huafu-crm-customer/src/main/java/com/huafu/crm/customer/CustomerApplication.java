package com.huafu.crm.customer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.huafu.crm")
@MapperScan({"com.huafu.crm.customer.mapper", "com.huafu.crm.common.mapper"})
public class CustomerApplication {
    public static void main(String[] args) { SpringApplication.run(CustomerApplication.class, args); }
}
