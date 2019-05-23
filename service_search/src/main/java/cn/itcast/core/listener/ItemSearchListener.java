package cn.itcast.core.listener;

import cn.itcast.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 创建自定义监听器
 * 根据商品id从数据库中获取详细数据
 * 将商品的详细数据更新到solr索引库中 供前台使用
 */
public class ItemSearchListener implements MessageListener {
    /**
     *
     */
    @Autowired
    SolrManagerService solrManagerService;

    @Override
    public void onMessage(Message message) {
        //将jdk底层的文本信息转换为activeMQ的文本消息对象
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String goodsId = atm.getText();
            solrManagerService.addItemToSolr(Long.parseLong(goodsId));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
