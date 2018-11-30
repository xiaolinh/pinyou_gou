package cn.itcast.core.service.User;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    /**
     * 发送验证码
     * @param phone
     */
    public void sendCode(String phone);

    /**
     * 注册用户
     * @param smscode
     * @param user
     */
    void regist(String smscode, User user);
}
