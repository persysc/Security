package pers.ysc.config;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pers.ysc.common.JwtAuthenticationTokenFilter;
import pers.ysc.common.lang.Result;
import pers.ysc.component.AdminDetailsImpl;

import java.io.PrintWriter;

/**
 * @Date:2021/1/31
 * @describe:
 * @author:ysc
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()// 基于token,所以不需要session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //通过请求地址与角色的联系进行权限控制 从上往下匹配(顺序很重要)
                /*.antMatchers("/admin/**").hasRole("admin")
                .antMatchers("/user/**").hasRole("user")*/
                //放行注册
                .antMatchers(HttpMethod.GET, // 允许对于网站静态资源的无授权访问
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/swagger-resources/**",
                        "/v2/api-docs/**"
                )
                .permitAll()
                .antMatchers("/security/register", "/security/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                //自定义登录页面并放行
                .loginPage("/login.html")
                //自定义登录逻辑并放行
                /*.loginProcessingUrl("/security/login")*/
                //登录成功跳转路径
                //.successForwardUrl("/toMain")
                //也是登录成功  客户端跳转 重定向原理(如果客户访问/hello 但是要先登录 登录后会重定向到hello(后参默认是false的情况))
                /*.defaultSuccessUrl("/toMain")*/
                //前后端分离成功登录的实现
                .successHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(authentication.getPrincipal()));
                    out.flush();
                    out.close();
                })
                //前后端分离失败登录的实现
                .failureHandler((req, resp, exception) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(exception.getMessage() + "密码错误!!"));
                    out.flush();
                    out.close();
                })
                //登录失败跳转路径
                /*.failureForwardUrl("/toError")*/
                .and()
                .logout()
                //注销登录路径
                .logoutUrl("/logout")
                //post方式注销登录路径
                /*.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))*/
                //注销成功跳转到登录页面
                /*.logoutSuccessUrl("/login.html")*/
                //前后端分离 注销成功的实现
                .logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString("已成功注销"));
                    out.flush();
                    out.close();
                })
                .and()
                //关闭csrf
                .csrf().disable();
        // 禁用缓存
        http.headers().cacheControl();
        // 添加JWT filter过滤验证
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        //前后端分离 //授权未通过 403等情况实现
        http.exceptionHandling()
                .accessDeniedHandler((req, resp, exception) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.println(JSONUtil.parse(Result.fail("权限不足!")));
                    out.flush();
                    out.close();
                })
                //未登录实现
                .authenticationEntryPoint((req, resp, exception) -> {
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("application/json");
                    resp.getWriter().println(JSONUtil.parse(Result.fail(exception.getMessage() + "您尚未登录!,请登录")));
                    resp.getWriter().flush();
                });
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //放行前端静态资源
        web.ignoring().antMatchers("/js/**", "/images/**", "/css/**");
    }

    /* @Override
     @Bean
     protected UserDetailsService userDetailsService() {
         //基于内存实现两个用户
         InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
         manager.createUser(User.withUsername("admin").password(new BCryptPasswordEncoder().encode("123")).roles("admin").build());
         manager.createUser(User.withUsername("abc").password(new BCryptPasswordEncoder().encode("123")).roles("abc").build());
         return manager;
     }

     @Override
     protected void configure(AuthenticationManagerBuilder auth) throws Exception {
         //配置登录逻辑
         auth.userDetailsService(userDetailsService())
                 //配置密码相关
                 .passwordEncoder(passwordEncoder());
     }*/
    @Bean
    protected UserDetailsService userDetailsService() {
        return new AdminDetailsImpl();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //配置登录逻辑
        auth.userDetailsService(userDetailsService())
                //配置密码相关
                .passwordEncoder(passwordEncoder());
    }


    //权限继承
    @Bean
    RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        //表明 admin也拥有user的权限
        hierarchy.setHierarchy("ROLE_admin > ROLE_user");
        return hierarchy;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        /*return NoOpPasswordEncoder.getInstance();*/
        return new BCryptPasswordEncoder();
    }

    //Jwt权限过滤器
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    //authenticationManager
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
