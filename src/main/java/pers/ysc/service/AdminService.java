package pers.ysc.service;

import pers.ysc.common.lang.Result;
import pers.ysc.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 关注公众号：MarkerHub
 * @since 2021-01-31
 */
public interface AdminService extends IService<Admin> {
    Admin register(Admin umsAdminParam);

    Result generateAuthCode(String telephone);

    Result verifyAuthCode(String telephone, String authCode);

    String login(String username, String password);
}


