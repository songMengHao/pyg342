package cn.itcast.core.listener;

import cn.itcast.core.service.PageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

public class PageListener implements MessageListener {
    @Autowired
    private PageService pageService;

    @Override
    public void onMessage(Message message) {
        //将jdk底层的消息对象转化为activeMQ信息对象
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String goodsId = atm.getText();
            Map<String, Object> goodsMap = pageService.findGoodsData(Long.parseLong(goodsId));
            pageService.createStatic(Long.parseLong(goodsId), goodsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
