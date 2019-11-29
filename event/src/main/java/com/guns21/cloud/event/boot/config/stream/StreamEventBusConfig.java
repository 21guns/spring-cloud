package com.guns21.cloud.event.boot.config.stream;

import com.guns21.cloud.event.stream.StreamEventBus;
import org.springframework.context.annotation.Bean;

/**
 * Created by jliu on 2017/6/6.
 */
//@Configuration
//@EnableAsync
public class StreamEventBusConfig {

    @Bean
    public StreamEventBus eventBus() {
        return new StreamEventBus();
    }

}
