package pers.ysc;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64Codec;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.Date;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
        PasswordEncoder pe = new BCryptPasswordEncoder();
        String encode = pe.encode("123");
        System.out.println(encode);
        boolean matches = pe.matches("123", encode);
        System.out.println(matches);
    }

    @Test
    void testJwt() {
        long l = System.currentTimeMillis();
        //1分钟过期时间
        long exp = l + 60 * 1000;
        JwtBuilder builder = Jwts.builder()
                //第二部分 有效信息中的jti :888
                .setId("888")
                //第二部分  所面向的用户 sub
                .setSubject("rose")
                // 第二部分  签发时间
                .setIssuedAt(new Date())
                //加密算法 和 盐
                .signWith(SignatureAlgorithm.HS256, "xxxx")
                //设置过期时间
                .setExpiration(new Date(exp))
                //自定义声明参数是Map
                /*.addClaims()*/
                //也是自定义声明
                .claim("logo", "baoshijie")
                .claim("game", "只狼");
        String token = builder.compact();
        System.out.println(token);
        String[] split = token.split("\\.");
        for (String test : split) {
            //解码
            System.out.println(Base64Codec.BASE64.decodeToString(test));
        }
        /*Claims 就是第二部分的荷载*/
        //校验 解析
        Claims claims = Jwts.parser()
                .setSigningKey("xxxx")
                .parseClaimsJws(token)
                .getBody();
        //解析出第二部分有效载荷的信息
        System.out.println("jti------" + claims.getId() +
                "\n" + "sub------" + claims.getSubject() +
                "\n" + "iat------" + claims.getIssuedAt() +
                "\n" + "exp------" + claims.getExpiration() +
                "\n" + "game------" + claims.get("game") +
                "\n" + "logo------" + claims.get("logo"));
    }
}
