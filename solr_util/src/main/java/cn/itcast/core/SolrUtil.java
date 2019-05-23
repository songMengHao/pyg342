package cn.itcast.core;


import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 将库存表中审核通过的数据导入到solr索引库中
 */
@Component
public class SolrUtil {
    @Autowired
    ItemDao itemDao;
    @Autowired
    SolrTemplate solrTemplate;

    public void importItemToSolr() {
        //创建查询对象
        ItemQuery itemQuery = new ItemQuery();
        //创建sql语句查询对象
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        //查询审核状态为1的
        criteria.andStatusEqualTo("1");
        //得到库存集合数据
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        //遍历
        if (itemList != null) {
            for (Item item : itemList) {
                //获取每一个库存对象的规格json字符串
                String spec = item.getSpec();
                //将json解析为map
                Map<String, String> jsonMap = JSON.parseObject(spec, Map.class);
                //将map数据放入Item的规格中
                item.setSpecMap(jsonMap);
            }
        }
        //库存集合到索引库中
        solrTemplate.saveBeans(itemList);
        //提交
        solrTemplate.commit();

    }

    /**
     * 这个工具项目的入口
     */
    public static void main(String[] args) {
        //创建spring运行环境
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        //获取当前实例化对象
        SolrUtil itemToSolr = (SolrUtil)context.getBean("solrUtil");
        //运行
        itemToSolr.importItemToSolr();

    }

}
