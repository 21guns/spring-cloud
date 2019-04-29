package com.guns21.cloud.gateway.filter.spring;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 自定义GatewayFilter需要在RouteLocator中配置为具体Route项加入
 */
public class PreGatewayFilterFactory extends AbstractGatewayFilterFactory<PreGatewayFilterFactory.Config> {

	public PreGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		// grab configuration from Config object
		return (exchange, chain) -> {
            //If you want to build a "pre" filter you need to manipulate the
            //request before calling change.filter
            ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            //use builder to manipulate the request
			ServerHttpRequest request = exchange.getRequest();
			return chain.filter(exchange.mutate().request(request).build());
		};
	}

	public static class Config {
        //Put the configuration properties for your filter here
	}

}