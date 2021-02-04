package pers.ysc.component;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pers.ysc.entity.Admin;
import pers.ysc.entity.Permission;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Date:2021/1/7
 * @describe:
 * @author:ysc
 */
public class AdminUserDetails implements UserDetails {
    private Admin umsAdmin;
    private List<Permission> permissionList;

    public AdminUserDetails(Admin umsAdmin, List<Permission> permissionList) {
        this.umsAdmin = umsAdmin;
        this.permissionList = permissionList;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //返回当前用户的权限
        return permissionList.stream()
                //过滤掉permission对象value值为空的对象
                .filter(permission -> permission.getValue() != null)
                //将premission对象的value值作为SimpleGrantedAuthority 即当做一个权限对象
                .map(permission -> new SimpleGrantedAuthority(permission.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return umsAdmin.getPassword();
    }

    @Override
    public String getUsername() {
        return umsAdmin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
