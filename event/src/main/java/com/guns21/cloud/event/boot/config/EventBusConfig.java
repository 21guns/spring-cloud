package com.guns21.cloud.event.boot.config;

import com.guns21.cloud.event.StreamEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jliu on 2017/6/6.
 */
@Configuration
//@EnableAsync
public class EventBusConfig {

    @Bean
    public StreamEventBus eventBus() {
        return new StreamEventBus();
    }

}
