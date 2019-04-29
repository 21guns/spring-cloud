package com.guns21.cloud.gateway.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "com.ktjr.ddhc.security.permit")
public  class ApiGatewayConfig {

    private List<String> pages = new ArrayList<>();

    public List<String> getPages() {
        return pages;
    }
    @Bean
    public ErrorWebExceptionHandler myExceptionHandler() {
        return new ApiGatewayErrorWebExceptionHandler();
    }

    @Bean
    public RedisTemplate<String, Object> authRedisTemplate(RedisConnectionFactory factory, RedisSerializer springSessionDefaultRedisSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setDefaultSerializer(springSessionDefaultRedisSerializer);
        return template;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource);
    }


//    @Bean
//    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
//        ObjectMapper objectMapper = objectMapper();
////        for (SpringSessionRedisSerializerObjectMapperConfigure configurer : configurers) {
////            configurer.configureObjectMapper(objectMapper);
////        }
//        return new GenericJackson2JsonRedisSerializer(objectMapper);
//    }
//
//    ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
////        mapper.registerModules(SecurityJackson2Modules.getModules(this.loader));
////        mapper.registerModule(new AuthenticationJasksonModue());
//        return mapper;
//    }
}
