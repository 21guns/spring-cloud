package com.guns21.cloud.gateway.boot.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.ktjr.ddhc.service")
public class FeginServiceConfig {

    @Setter
    private String permissionUrl;

//    @Bean
//    public PermissionService dockingOrderService(ObjectMapper objectMapper) {
//        return Feign.builder()
//                .encoder(new JacksonEncoder(objectMapper))
//                .decoder(new JacksonDecoder(objectMapper))
//                .target(PermissionService.class, permissionUrl);
//    }
}
