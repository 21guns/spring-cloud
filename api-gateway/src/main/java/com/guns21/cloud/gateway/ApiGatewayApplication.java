package com.guns21.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用@EnableZuulProxy注解激活zuul。
 * 跟进该注解可以看到该注解整合了@EnableCircuitBreaker、@EnableDiscoveryClient，是个组合注解，目的是简化配置。
 *
 * @author eacdy
 */
//@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.guns21"})
//@EnableZuulProxy
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

//  @Bean
//  public ExampleFilter authenticationFilter(){
//    return new ExampleFilter();
//  }

//    @Bean
//    public AuthFilter authFilter() {
//        return new AuthFilter();
//    }
}
