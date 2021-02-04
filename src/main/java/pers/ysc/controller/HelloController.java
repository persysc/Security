package pers.ysc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Date:2021/1/31
 * @describe:
 * @author:ysc
 */
@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String hello() {
        return "HelloWorld";
    }

    @RequestMapping("/toMain")
    public String success() {
        return "成功了";
    }

    @RequestMapping("/toError")
    public String error() {
        return "失败了";
    }

    @GetMapping("/admin/hello")
    public String admin() {
        return "admin";
    }

    @GetMapping("/user/hello")
    public String user() {
        return "user";
    }
}
