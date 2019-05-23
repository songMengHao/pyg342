package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class PageServiceImpl implements PageService, ServletContextAware {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private ServletContext servletContext;

    @Override
    public Map<String, Object> findGoodsData(Long goodsId) {
        //新建Map集合用来存储
        HashMap<String, Object> rootMap = new HashMap<>();
        //根据商品id获取商品对象
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        //根据商品id获取商品详情对象
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        //根据商品id获取库存集合对象
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        //根据商品中的对应的分类id,找到对应的分类对象(查三次)并且将这三个名字封装到rootMap中
        if (goods != null) {
            String name1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName();
            rootMap.put("itemCat1", name1);
            String name2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName();
            rootMap.put("itemCat2", name2);
            String name3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName();
            rootMap.put("itemCat3", name3);
        }
        //分装数据然后返回
        rootMap.put("goods", goods);
        rootMap.put("goodsDesc", goodsDesc);
        rootMap.put("itemList", itemList);

        return rootMap;
    }

    @Override
    public void createStatic(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //获模板初始化对象
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        // 加载模板获取模板对象
        Template template = configuration.getTemplate("item.ftl");
        // 生成后的页面名称
        String path = goodsId + ".html";
        // 设置静态页面生成后的位置,将相对路径转化为绝对路径
        String realPath = getRealPath(path);
        // 创建输出流
        Writer writer = new OutputStreamWriter(new FileOutputStream(new File
                (realPath)), "utf-8");
        // 生成

        template.process(rootMap,writer);

        // 关闭流
        writer.close();

    }
//实例化 servletContext
    @Override
    public void setServletContext(ServletContext servletContext) {
     this.servletContext=servletContext;
    }
    //将相对路径转化为绝对路径
    private String getRealPath(String path){
        String realPath = servletContext.getRealPath(path);
        return realPath;
    }



}
