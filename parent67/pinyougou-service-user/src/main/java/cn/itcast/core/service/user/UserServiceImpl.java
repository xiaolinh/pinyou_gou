package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.md5.MD5Util;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.User.UserService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination smsDestination;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserDao userDao;

    /**
     * 发送短信验证码
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {

        // 封装的数据：手机号、随机生成验证码、签名、模板
        final String code = RandomStringUtils.randomNumeric(6);

        //将验证码保存发哦redis中
        redisTemplate.boundValueOps(phone).set(code);
        //设置过期时间
        redisTemplate.boundValueOps(phone).expire(10, TimeUnit.MINUTES);

        // 将数据发送到mq中

        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();

                mapMessage.setInt("appid", 1400163940);
                mapMessage.setString("appkey", "94af9aae1f70090274f2eb7cb046d249");
                mapMessage.setString("code", code);
                mapMessage.setString("smsSign", "xiaolin7");
                mapMessage.setString("phone", phone);
                mapMessage.setInt("templateId", 238213);
                System.out.println(code);
                return mapMessage;
            }
        });
    }

    /**
     * 用户注册
     * @param smscode
     * @param user
     */
    @Transactional
    @Override
    public void regist(String smscode, User user) {
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();

        if (null != smscode && code.equals(smscode)) {
            // 保存用户
            String password = MD5Util.MD5Encode(user.getPassword(), null);
            user.setPassword(password);
            user.setCreated(new Date());
            user.setUpdated(new Date());
            userDao.insertSelective(user);
        } else {
            throw new RuntimeException("验证码不正确");
        }

    }
}
