package pers.ysc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import pers.ysc.common.lang.Result;
import pers.ysc.common.utils.JwtTokenUtil;
import pers.ysc.entity.Admin;
import pers.ysc.mapper.AdminMapper;
import pers.ysc.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pers.ysc.service.RedisService;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ysc
 * @since 2021-01-31
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Value("${redis.key.prefix.authCode}")
    private String REDIS_TEL_CODE;
    @Value("${redis.key.expire.authCode}")
    private Long EXPIRE_TIME;
    @Autowired
    RedisService redisService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);


    //登录功能
    @Override
    public String login(String username, String password) {
        String token = null;
        try {
            //确认确实存在有用户名为username的用户
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            //再进行密码的验证 matches(明文,加密后)
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("密码不正确");
            }
            //封装成UsernamePasswordAuthenticationToken对象
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            //将AuthenticationManager返回的Authentication对象赋予给当前的SecurityContext。
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //生成token
            token = jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }


    //注册
    public Admin register(Admin umsAdminParam) {
        Admin umsAdmin = new Admin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        LocalDateTime now = LocalDateTime.now();
        umsAdmin.setCreateTime(now);
        //查询是否有相同用户名的用户
        Admin admin = this.getOne(new QueryWrapper<Admin>().eq("username", umsAdmin.getUsername()));
        if (admin != null) {
            return null;
        }
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        //加入到用户表中
        this.save(umsAdmin);
        //返回一个用户
        return umsAdmin;
    }

    //生成验证码并存储
    @Override
    public Result generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            //nextInt的一般是指定范围内的int值被伪随机生成并返回。 所有bound可能的int值以（近似）等概率产生。
            sb.append(random.nextInt(10));
        }
        //验证码绑定手机号并存储到redis
        redisService.set(REDIS_TEL_CODE + telephone, sb.toString());
        redisService.expire(REDIS_TEL_CODE + telephone, EXPIRE_TIME);
        return Result.success(200, "获取验证码成功", sb.toString());
    }

    //对输入的验证码进行校验
    @Override
    public Result verifyAuthCode(String telephone, String authCode) {
        if (StringUtils.isEmpty(authCode)) {
            return Result.fail("请输入验证码");
        }
        String realAuthCode = redisService.get(REDIS_TEL_CODE + telephone);
        boolean result = authCode.equals(realAuthCode);
        if (result) {
            return Result.success("验证码校验成功");
        } else {
            return Result.fail("验证码不正确");
        }
    }
}
