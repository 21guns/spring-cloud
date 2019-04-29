package com.guns21.cloud.gateway.boot.config;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.guns21.domain.result.light.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.List;

public class ApiGatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayErrorWebExceptionHandler.class);
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        if (throwable instanceof ConnectException) {
            LOGGER.error("Connect Exception {}", throwable.getMessage());
            Result<Object> fail = Result.fail("服务器未启动.");
            DataBuffer write = serverWebExchange.getResponse().bufferFactory()
                    .allocateBuffer()
                    .write(JSON.toJSONBytes(fail));
            return serverWebExchange.getResponse().writeWith(Flux.just(write));
        }
        LOGGER.error("handle throwable ", throwable);
        Result<Object> fail = Result.fail(firstThrowableAsString(throwable));
        DataBuffer write = serverWebExchange.getResponse().bufferFactory()
                .allocateBuffer()
                .write(JSON.toJSONBytes(fail));
        return serverWebExchange.getResponse().writeWith(Flux.just(write));
    }

    private String firstThrowableAsString(Throwable throwable) {
        List<Throwable> causalChain = Throwables.getCausalChain(throwable);
        String causal = causalChain.size() >= 1 ? Throwables.getStackTraceAsString(causalChain.get(0)) : "空异常信息";
        return StringUtils.left(causal, 400);
    }
}
