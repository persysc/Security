package pers.ysc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pers.ysc.common.lang.Result;
import pers.ysc.component.AdminLoginParam;
import pers.ysc.dao.AdminPermissionMapper;
import pers.ysc.entity.Admin;
import pers.ysc.entity.Permission;
import pers.ysc.service.AdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ysc
 * @since 2021-01-31
 */
@RestController
@Api(tags = "AdminController", description = "用户管理")
@RequestMapping("/security")
public class AdminController {
    @Autowired
    AdminService adminService;
    @Autowired
    AdminPermissionMapper adminPermission;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    @ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody AdminLoginParam umsAdminLoginParam, BindingResult result) {
        LOGGER.info("已经开始登陆了");
        String token = adminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        LOGGER.info("登陆了");
        if (token == null) {
            return Result.fail("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return Result.success(tokenMap);
    }

    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Result register(@RequestBody Admin umsAdminParam, BindingResult result) {
        Admin umsAdmin = adminService.register(umsAdminParam);
        if (null == umsAdmin) {
            return Result.fail("已经有此用户名了");
        }
        return Result.success(umsAdmin);
    }

    @ApiOperation("获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    public Result getAuthCode(@RequestParam String telephone) {
        return adminService.generateAuthCode(telephone);
    }

    @ApiOperation("判断验证码是否正确")
    @RequestMapping(value = "/verifyAuthCode", method = RequestMethod.POST)
    public Result updatePassword(@RequestParam String telephone,
                                 @RequestParam String authCode) {
        return adminService.verifyAuthCode(telephone, authCode);
    }

    @ApiOperation("查看某个用户的权限")
    @RequestMapping(value = "/checkPermission", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('check:permission')")
    public Result checkPermission(@RequestParam String username) {
        Admin admin = adminService.getOne(new QueryWrapper<Admin>().eq("username", username));
        Assert.notNull(admin, "该用户不存在!");
        List<Permission> adminPer = adminPermission.getPermissionList(admin.getUsername());
        return Result.success(adminPer);
    }
}
