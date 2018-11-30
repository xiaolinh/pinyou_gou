package cn.itcast.core.controller;

import cn.itcast.core.Phone.PhoneFormatCheckUtils;
import cn.itcast.core.md5.MD5Util;
import cn.itcast.core.pojo.entity.Result;


import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.User.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode.do")
    public Result sendCode(String phone) {
        try {
            if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                return new Result(false, "手机号不合格");
            }
            userService.sendCode(phone);
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }

    /**
     * 用户注册
     * @param smscode
     * @param user
     * @return
     */
    @RequestMapping("/add.do")
    public Result regist(String smscode ,@RequestBody User user) {
        try {
            userService.regist(smscode, user);
            return new Result(true, "注册成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }

    }
}
