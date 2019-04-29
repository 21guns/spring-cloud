package com.guns21.cloud.gateway.filter.spring;

import com.alibaba.fastjson.JSON;
import com.guns21.cloud.gateway.GatewayConstants;
import com.guns21.cloud.gateway.boot.config.ApiGatewayConfig;
import com.guns21.cloud.gateway.dto.PermissionDTO;
import com.guns21.cloud.gateway.service.PermissionService;
import com.guns21.common.util.ObjectUtils;
import com.guns21.data.domain.result.MessageResult;
import com.guns21.domain.result.light.Result;
import com.guns21.user.login.domain.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class AuthGlobalRouteFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthGlobalRouteFilter.class);

    @Resource(name = "authRedisTemplate")
    private RedisTemplate<String, Map<String, Object>> template;

    @Autowired
    private ApiGatewayConfig securityPermitConfig;

    @Autowired
    private MessageSourceAccessor messageSourceAccessor;

    @Autowired
    private PermissionService permissionService;

    private PathPatternParser pathPatternParser = new PathPatternParser();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        HttpMethod method = request.getMethod();
        LOGGER.info("route url {}:[{}] with headers[{}]", request.getMethod(), path.value(), request.getHeaders());
        /*
            0.不需要设置权限的url
         */
        boolean b = securityPermitConfig.getPages().stream()
                .anyMatch(url -> pathPatternParser.parse(url)
                        .matches(path));
        if (b) {
            return chain.filter(exchange);
        }

        /*
            1.获取token，如果token存在获取当前用户的角色信息
         */
        String token = request.getHeaders().getFirst(GatewayConstants.HEADER_X_AUTH_TOKEN);
        if (StringUtils.isEmpty(token)) {
            Result<Object> fail401 = Result.fail401(messageSourceAccessor.getMessage("com.guns21.security.message.login.commence", "请登录"));
            DataBuffer write = exchange.getResponse().bufferFactory()
                    .allocateBuffer()
                    .write(JSON.toJSONBytes(fail401));
            LOGGER.warn("Current User hasn't Authenticated.");
            return exchange.getResponse().writeWith(Flux.just(write));

        }
        BoundHashOperations<String, String, Object> stringObjectObjectBoundHashOperations = template.boundHashOps(GatewayConstants.DEFAULT_NAMESPACE + token);
        Object loginUser = stringObjectObjectBoundHashOperations.get(GatewayConstants.LOGIN_USER);
        if (Objects.isNull(loginUser)) {
            //902重新登录
            Result<Object> fail902 = Result.fail("902", messageSourceAccessor.getMessage("com.guns21.security.message.relogin", "请重新登录"));
            DataBuffer write = exchange.getResponse().bufferFactory()
                    .allocateBuffer()
                    .write(JSON.toJSONBytes(fail902));

            LOGGER.warn("Current User session is expires,pls relogin.");
            return exchange.getResponse().writeWith(Flux.just(write));
        }

        Set<String> loginUserRoles = Collections.emptySet();
        if (loginUser instanceof UserInfo) {
            UserInfo salesmanUserInfo = (UserInfo) loginUser;
            loginUserRoles = salesmanUserInfo.getRoles().stream()
                    .map(s ->s.getName())
                    .collect(Collectors.toSet());
        } else {
            LOGGER.warn("login user class {}", loginUser.getClass().getName());
        }
        /*
            2.获取该url对应的具体角色的列表，
         */
        Set<String> urlRoles = getUrlRoles(loginUser, path, method);
        /*
            3.比较1和2中的角色是否包含
         */
        //取交集
        loginUserRoles.retainAll(urlRoles);
        if (loginUserRoles.size() == 0) {
            //没有权限
            Result<Object> fail403 = Result.fail403("禁止访问");
            DataBuffer write = exchange.getResponse().bufferFactory()
                    .allocateBuffer()
                    .write(JSON.toJSONBytes(fail403));
            LOGGER.warn("Current User Forbidden[403] url.");
            return exchange.getResponse().writeWith(Flux.just(write));
        }
        return chain.filter(exchange);
    }

    private Set<String> getUrlRoles(Object loginUser, PathContainer path, HttpMethod method) {
        Set<String> urlRoles = Collections.emptySet();
        if (loginUser instanceof UserInfo) {
            //op 多多关照
            // key:GET:/op/v1/stores/check; value:\"121234,ADMIN,OUTSOURCED_SALESMAN,SALESMAN,STORE_MANAGER\"
            Map<Object, Object> roleUrlMapping = template.opsForHash().entries(GatewayConstants.PERMISSION_REDIS_KEY);
            if (ObjectUtils.isEmpty(roleUrlMapping)) {
                roleUrlMapping = initRedisPermission();
            }

            urlRoles = collectRolesFromPath(roleUrlMapping, path, method);

            //TODO 针对新加url时，需要重新加载redis,当redis中匹配改url的角色中只有SUPER_ADMIN
            if (ObjectUtils.nonEmpty(urlRoles)
                    && urlRoles.size() == 1
                    && urlRoles.contains(GatewayConstants.ROLE_SUPER_ADMIN)) {
                LOGGER.info("redis中匹配[{}]的角色中只有(SUPER_ADMIN),尝试重新加载(permission_redis_key)中的数据", path.value());
                roleUrlMapping = initRedisPermission();
                urlRoles = collectRolesFromPath(roleUrlMapping, path, method);
            }
        } else if (
//                loginUser instanceof UserInfoExtension ||
                loginUser instanceof UserInfo) { //fixme 用手机号登录时loginUser class is UserInfo
            //多多好车端登录的用户都是NORMAL角色，对多多好车权限校验的默认处理，
            urlRoles = Collections.singleton("NORMAL");
        } else {
            LOGGER.warn("login user class {}", loginUser.getClass().getName());
        }
        return urlRoles;
    }

    private Map<Object, Object> initRedisPermission() {
        Map<Object, Object> roleUrlMapping = null;
        MessageResult<List<PermissionDTO>> messageResult = permissionService.listRoles();
        if (messageResult.getSuccess()) {
            roleUrlMapping = messageResult.getData().stream()
                    .filter(permissionDTO -> !StringUtils.isEmpty(permissionDTO.getPermissionUrl()))
                    .collect(Collectors.toMap(PermissionDTO::getUniqueKey, PermissionDTO::getPermissionName, (s, a) -> s + "," + a));


            template.opsForHash().putAll(GatewayConstants.PERMISSION_REDIS_KEY, roleUrlMapping);
        } else {
            LOGGER.warn("get url's role fail, reason [{}]", messageResult);
        }
        return roleUrlMapping;
    }

    /**
     * 根据Path返回对应的role,以,分隔
     * @param roleUrlMapping
     * @param path
     * @param method
     * @return
     */
    private Set<String> collectRolesFromPath(Map<Object, Object> roleUrlMapping, PathContainer path, HttpMethod method) {
        return roleUrlMapping.entrySet().stream()
                .filter(entry -> {
//                        LOGGER.info(entry.getKey().toString());
                        /*
                            适配以前系统permission_redis_key的key格式
                            "\"{[/op/v1/customers/application],methods=[GET]}\""
                            "[\"java.util.ArrayList\",[\"ADMIN\",\"RISK\",\"TELESALES\",\"SUPER_ADMIN\"]]"
                         */
                        if (StringUtils.isNotBlank(entry.getKey().toString())
                            && entry.getKey().toString().startsWith("\"{[")) {
                            return false;
                        }
                        return true;
                    })
                .filter(entry -> pathPatternParser.parse(PermissionDTO.parseUrl(entry.getKey().toString()))
                        .matches(path))
                .filter(entry -> {
                    String requestMethod = PermissionDTO.parseRequestMethod(entry.getKey().toString());
                    return PermissionDTO.FULL_ACCESS.equalsIgnoreCase(requestMethod)
                            || method.matches(requestMethod); })

//                    .filter(entry -> {
//                        LOGGER.info(entry.getKey().toString());
//                        return true;
//                    })
                .map(entry -> entry.getValue().toString())
                .flatMap(roles -> Stream.of(roles.split(",")))
                .collect(Collectors.toSet());
    }
}