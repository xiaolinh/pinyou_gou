package cn.itcast.core.listener;


import cn.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


public class ItemDeleteListener implements MessageListener {
    @Resource
    ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            // 监听容器获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            // 处理业务
            itemSearchService.deleteItemFromSolr(Long.parseLong(id));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
