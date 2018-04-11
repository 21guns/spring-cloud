package com.guns21.bus.config;

import com.guns21.bus.endpoint.DatasourceBusEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusConfiguration {
    @Bean
    public DatasourceBusEndpoint datasourceBusEndpoint() {
        return new DatasourceBusEndpoint();
    }


}