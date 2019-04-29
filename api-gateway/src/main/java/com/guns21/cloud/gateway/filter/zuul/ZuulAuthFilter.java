package com.guns21.cloud.gateway.filter.zuul;//package com.ktjr.ddhc.gateway.filter;
//
//import com.guns21.data.domain.result.MessageResult;
//import com.guns21.domain.result.light.Result;
//import com.guns21.http.HttpStatus;
//import com.guns21.servlet.util.ResponseUtils;
//import com.guns21.user.login.domain.UserInfo;
//import com.ktjr.ddhc.gateway.GatewayConstants;
//import com.ktjr.ddhc.gateway.api.dto.PermissionDTO;
//import com.ktjr.ddhc.gateway.boot.config.SecurityPermitConfig;
//import com.ktjr.ddhc.gateway.service.PermissionService;
//import com.ktjr.ddhc.login.domin.SalesmanUserInfo;
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.support.MessageSourceAccessor;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import java.io.*;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
//public class AuthFilter extends ZuulFilter {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
//
//    @Resource(name = "authRedisTemplate")
//    private RedisTemplate<String,  Map<String, Object>> template;
//
//    @Autowired
//    private SecurityPermitConfig securityPermitConfig;
//
//    @Autowired
//    private MessageSourceAccessor messageSourceAccessor;
//
//    @Autowired
//    private PermissionService permissionService;
//
//    @Override
//    public Object run() {
//        /**
//         * TODO 多多关照中角色变更时，需要清除key
//         */
//        RequestContext context = RequestContext.getCurrentContext();
//        HttpServletRequest request = context.getRequest();
//        LOGGER.debug("AuthFilter: request url : {} " , request.getRequestURI());
//        /*
//            0.不需要设置权限的url
//         */
//
//        boolean b = securityPermitConfig.getPages().stream()
//                    .anyMatch(url -> new AntPathRequestMatcher(url)
//                            .matches(request));
//        if (b) {
//            return context;
//        }
//
//        /*
//            1.获取token，如果token存在获取当前用户的角色信息
//         */
//        String token = request.getHeader(GatewayConstants.HEADER_X_AUTH_TOKEN);
//        if (StringUtils.isEmpty(token)) {
//            context.setSendZuulResponse(false);
//            context.setResponseStatusCode(HttpStatus.OK.value());
//            try {
//                ResponseUtils.writeResponse(context.getResponse(), Result.fail401(messageSourceAccessor.getMessage("com.guns21.security.message.login.commence", "请登录")));
//            } catch (IOException e) {
//            }
//            LOGGER.warn("Current User hasn't Authenticated.");
//            return context;
//        }
//        BoundHashOperations<String, String, Object> stringObjectObjectBoundHashOperations = template.boundHashOps(GatewayConstants.DEFAULT_NAMESPACE + token);
//        Object loginUser = stringObjectObjectBoundHashOperations.get(GatewayConstants.LOGIN_USER);
//        if (Objects.isNull(loginUser)) {
//            //902重新登录
//            context.setSendZuulResponse(false);
//            try {
//                ResponseUtils.writeResponse(context.getResponse(), Result.fail("902", messageSourceAccessor.getMessage("com.guns21.security.message.relogin", "请重新登录")));
//            } catch (IOException e) {
//            }
//            context.setResponseStatusCode(HttpStatus.OK.value());
//            LOGGER.warn("Current User session is expires,pls relogin.");
//            return context;
//        }
//
//        Set<String> loginUserRoles = Collections.emptySet();
//        if (loginUser instanceof UserInfo) {
//            UserInfo salesmanUserInfo = (UserInfo) loginUser;
//            loginUserRoles = salesmanUserInfo.getRoles().stream()
//                    .map(s ->s.getName())
//                    .collect(Collectors.toSet());
//        }
//        /*
//            2.获取该url对应的具体角色的列表，
//         */
//        Set<String> urlRoles = Collections.emptySet();
//
//        // key:GET:/op/v1/stores/check; value:\"121234,ADMIN,OUTSOURCED_SALESMAN,SALESMAN,STORE_MANAGER\"
//        Map<Object, Object> roleUrlMapping = template.opsForHash().entries(GatewayConstants.PERMISSION_REDIS_KEY);
//        if (Objects.isNull(roleUrlMapping) || roleUrlMapping.size() == 0) {
//            MessageResult<List<PermissionDTO>> messageResult = permissionService.listRoles();
//            if (messageResult.getSuccess()) {
//
//
//                roleUrlMapping = messageResult.getData().stream()
//                        .filter(permissionDTO -> !StringUtils.isEmpty(permissionDTO.getPermissionUrl()))
//                        .collect(Collectors.toMap(PermissionDTO::getUniqueKey, PermissionDTO::getPermissionName));
//
//                //TODO 多多好车端登录的用户都是NORMAL角色，对多多好车权限校验的默认处理，
//                PermissionDTO normal = PermissionDTO.builder()
//                        .permissionName("NORMAL")
//                        .permissionUrl("/**")
//                        .requestMethod(PermissionDTO.FULL_ACCESS)
//                        .build();
//                roleUrlMapping.put(normal.getUniqueKey(), normal.getPermissionName());
//                //end
//
//                template.opsForHash().putAll(GatewayConstants.PERMISSION_REDIS_KEY, roleUrlMapping);
//            } else {
//                LOGGER.warn("get url's role fail, reason [{}]", messageResult);
//            }
//        }
//
//        urlRoles = roleUrlMapping.entrySet().stream()
//                .filter(entry ->  new AntPathRequestMatcher(PermissionDTO.parseUrl(entry.getKey().toString()))
//                                    .matches(request))
////                .filter(entry -> {
////                    LOGGER.debug(entry.getKey().toString());
////                    return true;
////                })
//                .map(entry -> entry.getValue().toString())
//                .flatMap(roles -> Stream.of(roles.split(",")))
//                .collect(Collectors.toSet());
//
//        /*
//            3.比较1和2中的角色是否包含
//         */
//        //取交集
//        loginUserRoles.retainAll(urlRoles);
//        if (loginUserRoles.size() == 0) {
//            //没有权限
//            context.setSendZuulResponse(false);
//            context.setResponseStatusCode(HttpStatus.OK.value());
//            try {
//                ResponseUtils.writeResponse(context.getResponse(), Result.fail403("禁止访问"));
//            } catch (IOException e) {
//            }
//            LOGGER.warn("Current User Forbidden[403] url.");
//            return context;
//        }
//
//        return context;
//    }
//
//
//    /**
//     * filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
//     * pre：可以在请求被路由之前调用
//     * routing：在路由请求时候被调用
//     * post：在routing和error过滤器之后被调用
//     * error：处理请求时发生错误时被调用
//     *
//     * @return
//     */
//    @Override
//    public String filterType() {
//        return "pre";
//    }
//
//    /**
//     * filterOrder：通过int值来定义过滤器的执行顺序
//     *
//     * @return
//     */
//    @Override
//    public int filterOrder() {
//        return 0;
//    }
//
//    /**
//     * shouldFilter：返回一个boolean类型来判断该过滤器是否要执行，所以通过此函数可实现过滤器的开关。
//     * 我们直接返回true，所以该过滤器总是生效。
//     *
//     * @return
//     */
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//}
