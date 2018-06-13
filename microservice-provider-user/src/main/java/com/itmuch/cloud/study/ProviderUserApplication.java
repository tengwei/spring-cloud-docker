package com.itmuch.cloud.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@EnableDiscoveryClient
@SpringBootApplication
public class ProviderUserApplication {
  public static void main(String[] args) {
    SpringApplication.run(ProviderUserApplication.class, args);
  }
}
