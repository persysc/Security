package pers.ysc.component;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pers.ysc.dao.AdminPermissionMapper;
import pers.ysc.entity.Admin;
import pers.ysc.entity.Permission;
import pers.ysc.service.AdminService;

import java.util.List;

/**
 * @Date:2021/1/31
 * @describe:登录逻辑类
 * @author:ysc
 */

@Service
public class AdminDetailsImpl implements UserDetailsService {
    @Autowired
    private PasswordEncoder pw;
    @Autowired
    private AdminService adminService;
    @Autowired
    private AdminPermissionMapper adminPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1根据username查询数据库
        // 2根据查询的对象比较密码
        // 3返回用户对象

        //手动设置用户及对象
        /*System.out.println("执行自定义登录逻辑");
        if (!"admin".equals(username)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        String password = pw.encode("123");
        return new User("admin", password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin,normal,ROLE_admin,/main.html"));*/

        Admin admin = adminService.getOne(new QueryWrapper<Admin>().eq("username", username));
        if (admin != null) {
            //拿到权限
            List<Permission> permissionList = adminPermissionMapper.getPermissionList(admin.getUsername());
            return new AdminUserDetails(admin, permissionList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }
}
