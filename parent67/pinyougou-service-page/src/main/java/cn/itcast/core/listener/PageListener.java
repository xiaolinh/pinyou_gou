package cn.itcast.core.listener;

import cn.itcast.core.service.staticpage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {

    @Autowired
    private StaticPageService staticPageService;

    /*
     * (non-Javadoc)
     * <p>Title: onMessage</p>
     * <p>Description: 生成商品详情静页</p>
     * @param message
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        try {
            // 获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            //System.out.println("service-page-id:"+id);
            // 消费消息
            staticPageService.getHtml(id);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}

