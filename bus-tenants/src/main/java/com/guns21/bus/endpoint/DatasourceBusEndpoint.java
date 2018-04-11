package com.guns21.bus.endpoint;

import com.guns21.bus.event.AddDataSourceApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.ApplicationEventPublisher;

@Endpoint(id = "datasource")
public class DatasourceBusEndpoint {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @WriteOperation
    public void datasource(@Selector String dataSource) {
        this.applicationEventPublisher.publishEvent(new AddDataSourceApplicationEvent(dataSource));
    }
}