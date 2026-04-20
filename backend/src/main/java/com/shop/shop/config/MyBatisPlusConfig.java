package com.shop.shop.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.shop.shop.mapper")
public class MyBatisPlusConfig {
}
