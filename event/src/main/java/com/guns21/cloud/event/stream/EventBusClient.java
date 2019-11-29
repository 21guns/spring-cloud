package com.guns21.cloud.event.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface EventBusClient {
    String INPUT = "event-input";
    String OUTPUT = "event-output";

    @Output(OUTPUT)
    MessageChannel eventOutput();

    @Input(INPUT)
    SubscribableChannel eventInput();

}