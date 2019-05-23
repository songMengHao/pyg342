package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.PatternSyntaxException;

/**
 * 品优购用户注册
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    /**
     *
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
            //判断用户手机号是否合法
            boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
            if (!phoneLegal) {
                return new Result(false, "您的手机号违法!!!");
            }
            userService.sendCode(phone);
            return new Result(true, "注册成功");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }
    }

    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode) {
        try {
            //验证验证码是否正确
            if (smscode == null || "".equals(smscode)) {
                return new Result(false, "验证码有误");
            }
            boolean isCheck = userService.checkCode(user.getPhone(), smscode);
            if (!isCheck) {
                return new Result(false, "验证码填写错误!");
            }
            //2. 保存用户对象到数据库中完成注册
            userService.add(user);
            return new Result(true, "注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }
    }

}
