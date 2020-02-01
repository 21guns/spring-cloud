package com.guns21.cloud.event.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

/**
 * JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)  会添加一个属性，可以支持多态
 * "@class": "com.sifangting.content.api.dto.ContentWithContentFileAndColumnIdsAndCourseIdsDTO"
 * 使用方式:
 *  mixIn(BaseEvent.class, BaseEventMixin.class);
 */
public interface BaseEventMixin {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY) //都可以起作用,使用一个就行
        void setSource(Object source);
//        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY) //都可以起作用,使用一个就行
        Object getSource();

        @JsonIgnore
        Optional getValue();
}