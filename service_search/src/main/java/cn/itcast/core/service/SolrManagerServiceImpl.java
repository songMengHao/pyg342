package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import java.util.List;
import java.util.Map;
@Service
public class SolrManagerServiceImpl implements SolrManagerService{
    @Autowired
 private   ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;
    //上架商品
    @Override
    public void addItemToSolr(Long goodsId) {
        //根据库存id 得到库存集合
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        //条件查询对象
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> items = itemDao.selectByExample(itemQuery);
        if (items!=null){
            for (Item item : items) {
             Map<String,String>  jsonObject = JSON.parseObject(item.getSpec(),Map.class);
             item.setSpecMap(jsonObject);
            }
        }
        //保存库存集合到solr索引库中
        solrTemplate.saveBeans(items);
        //提交
        solrTemplate.commit();


    }
//下架商品
    @Override
    public void deleteItemByGoodsId(Long goodsId) {
        //创建查询对象
        SimpleQuery simpleQuery = new SimpleQuery();
        //创建条件查询对象
        Criteria criteria = new Criteria("item_goodsid").is(goodsId);
        simpleQuery.addCriteria(criteria);
        solrTemplate.delete(simpleQuery);
        solrTemplate.commit();

    }
}
