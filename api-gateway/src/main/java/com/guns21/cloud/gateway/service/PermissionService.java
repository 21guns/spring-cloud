package com.guns21.cloud.gateway.service;

import com.guns21.cloud.gateway.dto.PermissionDTO;
import com.guns21.data.domain.result.MessageResult;

import java.util.List;

/**
 * Created By guohang ON 2018/8/7
 */
//@Headers("Content-Type: application/json")
public interface PermissionService {


//    @RequestLine("GET /api/usermanage/v1/permission/list/roles")
    MessageResult<List<PermissionDTO>> listRoles();
}
