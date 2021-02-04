package pers.ysc.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pers.ysc.entity.Permission;

import java.util.List;

/**
 * @Date:2021/2/2
 * @describe:
 * @author:ysc
 */
public interface AdminPermissionMapper {
    List<Permission> getPermissionList(@Param("username") String username);
}
