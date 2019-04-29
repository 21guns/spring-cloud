package com.guns21.cloud.gateway.dto;

import com.guns21.common.util.ObjectUtils;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PermissionDTO implements Serializable {


    public static final String FULL_RESOURCE = "FULL";

    public static final String FULL_ACCESS = "FULL";

    private String permissionName;

    private String permissionUrl;

    private String requestMethod;

    public String getUniqueKey() {
        return  (ObjectUtils.hasText(this.requestMethod)? this.requestMethod : FULL_ACCESS)
                    + ":"
                    + (ObjectUtils.hasText(this.permissionUrl)? this.permissionUrl : FULL_RESOURCE);
    }

    public static String parseRequestMethod(String str){
        if (null == str || str.length() ==0) {
            return null;
        }
        String[] split = str.split(":");
        return split.length >= 1? split[0] : null ;
    }

    public static String parseUrl(String str){
        if (null == str || str.length() ==0) {
            return null;
        }
        String[] split = str.split(":");
        return split.length > 1? split[1] : "" ;
    }
}
