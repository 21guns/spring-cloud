package com.guns21.cloud.event.stream;

import com.guns21.event.EventBus;
import com.guns21.event.domain.AskEvent;
import com.guns21.event.domain.NotifyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by jliu on 2017/6/2.
 */
public class StreamEventBus implements EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamEventBus.class);

    @Value("${spring.application.name}")
    private String appName;
    @Autowired
    private EventProcessor eventProcessor;

    @Override
    public void publish(NotifyEvent event) {
        LOGGER.debug("publish event[{}]", event);
//        eventProcessor.sendMessage(event,event.getClass().getSimpleName());//每一个事件一个topic
//        eventProcessor.sendMessage(event,appName + CloudEventConstants.EVENT_TOPIC__SUFFIX);//每一个服务一个topic
        eventProcessor.sendMessage(event);
    }

    @Override
    public void publish(NotifyEvent notifyEvent, String destination) {
        LOGGER.debug("publish event[{}] to [{}]", notifyEvent, destination);
        eventProcessor.sendMessage(notifyEvent, destination);
    }


    @Override
    public void ask(AskEvent askEvent) {
        LOGGER.debug("ask event for {}", askEvent);
        throw new UnsupportedOperationException("unsupported ask");
    }
}
