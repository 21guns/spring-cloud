package com.guns21.cloud.gateway.service;

import com.guns21.session.domain.AuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hellonextone on 17/5/24.
 */
@Service
public class AuthService {
    private final String KEY_PREFIX = "exchange:system:role:";
    private final String IGNORE_URL_ROLE = "ignore_url";

    @Autowired
    private RedisTemplate<String, String> template;

    public boolean isValueInSet(String value, String key) {
        boolean result = false;
        SetOperations<String, String> ops = template.opsForSet();
        result = ops.isMember(key, value);

        return result;
    }


    public boolean isAuth(String sessionId, String url, AuthInfo authInfo) {
        boolean result = false;

        List<String> roles = new ArrayList<>();

        //把不需授权的url公用的角色添加到roles中
        roles.add(IGNORE_URL_ROLE);

        if (authInfo != null && authInfo.getRoles() != null) {
            roles.addAll(authInfo.getRoles());
        }

        String key = "";
        for (String role : roles) {
            key = KEY_PREFIX + role;
            if (isValueInSet(url, key)) {
                result = true;
                break;
            }
        }

        return result;
    }

}
