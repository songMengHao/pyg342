package cn.itcast.core.test;


import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import org.apache.zookeeper.data.Id;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class TestBrandDao {
    @Autowired
    private BrandDao brandDao;
    @Test
    public void run(){
        Brand brand = brandDao.selectByPrimaryKey(5l);
        System.out.println("~~~~~~~~~~"+brand);
    }
    @Test
    public void TestByQuery(){
            //创建查询对象
        BrandQuery brandQuery = new BrandQuery();
        //设置排序
        brandQuery.setOrderByClause("id  desc");
        //设置去重
        brandQuery.setDistinct(true);
        //创建where条件查询对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        criteria.andNameLike("%想%");
        List<Brand> brands = brandDao.selectByExample(brandQuery);
        for (Brand brand : brands) {
            System.out.println(brand);
        }
    }


}
