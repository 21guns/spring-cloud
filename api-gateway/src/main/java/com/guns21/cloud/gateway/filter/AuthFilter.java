package com.guns21.cloud.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.guns21.cloud.gateway.service.AuthService;
import com.guns21.session.domain.AuthInfo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by ljj on 17/5/24.
 */
public class AuthFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AuthService authService;

    /**
     * filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
     * pre：可以在请求被路由之前调用
     * routing：在路由请求时候被调用
     * post：在routing和error过滤器之后被调用
     * error：处理请求时发生错误时被调用
     *
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * filterOrder：通过int值来定义过滤器的执行顺序
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * shouldFilter：返回一个boolean类型来判断该过滤器是否要执行，所以通过此函数可实现过滤器的开关。
     * 我们直接返回true，所以该过滤器总是生效。
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        logger.info("AuthFilter: request url : " + request.getRequestURI().toString());
        ServletContext sc = request.getServletContext();

        ApplicationContext ctx = (ApplicationContext) WebApplicationContextUtils.getWebApplicationContext(sc);
        if (ctx != null && authService == null && ctx.getBean(AuthService.class) != null) {
            authService = (AuthService) ctx.getBean(AuthService.class);
        }

        //从session中读取用户授权信息
        HttpSession session = request.getSession(false);
        String sessionId = "", url = request.getRequestURI().toString();
        AuthInfo authInfo = null;
        if (session != null) {
            authInfo = JSONObject.toJavaObject((JSONObject) session.getAttribute("authInfo"), AuthInfo.class);
            sessionId = session.getId();
        }

        boolean isAuth = authService.isAuth(sessionId, url, authInfo);
        if (!isAuth) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(401);
            logger.info("Current User hasn't Authenticated.");
            //todo 返回信息
        }

        logger.info("authFilter sessinoId : " + sessionId);
        return ctx;
    }
}
