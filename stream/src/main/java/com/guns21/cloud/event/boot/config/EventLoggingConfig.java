package com.guns21.cloud.event.boot.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
//@ConditionalOnProperty(value = "com.ktjr.ddhc.config.stream.logging", matchIfMissing = true)
public class EventLoggingConfig {

    @Aspect
    @Configuration
    public class EventInputLogging {
        private Logger logger = LoggerFactory.getLogger("Event Log Consuming");

        @Before("@annotation(org.springframework.cloud.stream.annotation.StreamListener)")
        public void consumer(JoinPoint joinPoint){
            logger.info("consumer [{}] receive message: {} ", joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getArgs());
        }
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "*-output-event", order = -1)
    public ChannelInterceptor eventOutputLogging() {
        ChannelInterceptor eventOutputLogging = new ChannelInterceptor() {
            private Logger logger = LoggerFactory.getLogger("Event Log Producing");
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                logger.info("on channel [{}] send message: {} ",channel.toString(), message.getPayload());
                //just add valid
                /*BindException bindException = new BindException(updateEvent, "updateEvent");
                ValidationUtils.invokeValidator(validator, updateEvent.getSource(),bindException );*/
                return message;
            }

        };
        return eventOutputLogging;
    }
}
