package com.guns21.cloud.event.annotation;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jliu on 2017/6/8.
 */
@StreamListener
@Target( {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EventBusListener {
    @AliasFor(annotation = StreamListener.class, attribute = "value")
    String value()  default "";

    @AliasFor(annotation = StreamListener.class, attribute = "target")
    String target() default "";

    @AliasFor(annotation = StreamListener.class, attribute = "condition")
    String condition() default "";

    @AliasFor(annotation = StreamListener.class, attribute = "copyHeaders")
    String copyHeaders() default "true";
}
