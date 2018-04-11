package com.guns21.bus.event;

import java.util.EventObject;

/**
 * Created by jliu on 2017/6/1.
 */
public class AddDataSourceApplicationEvent extends EventObject {


    public AddDataSourceApplicationEvent(Object source) {
        super(source);
    }

    public String getDataSource() {
        return source.toString();
    }
}
