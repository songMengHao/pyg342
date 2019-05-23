package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private SolrManagerService solrManagerService;

    /**
     * 商品分页查询
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows) {
        //1. 获取当前登录用户的用户名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 向查询条件对象中添加当前登录用户的用户名作为查询条件
        goods.setSellerId(userName);
        //3. 进行分页查询
        PageResult pageResult = goodsService.search(goods, page, rows);
        return pageResult;
    }
    /**
     * 商品添加
     * @param goodsEntity 商品实体, 包含商品对象, 商品详情对象, 库存集合对象
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity) {
        try {
            //1. 获取当前登录用户的用户名
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            //2. 将用户身份信息放入商品对象中
            goodsEntity.getGoods().setSellerId(userName);
            //3. 保存
            goodsService.add(goodsEntity);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        GoodsEntity one = goodsService.findOne(id);
        return one;
    }

    /**
     * 商品状态修改
     * @param ids       商品id数组
     * @param status    状态码, 0未审核, 1审核通过, 2驳回
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            //1. 到数据库中更新商品的审核状态
            goodsService.updateStatus(ids, status);
            //2. 如果审核通过, 则根据商品id查询数据库商品的详细数据, 然后放入solr索引库中供搜索使用
            if ("1".equals(status) && ids != null) {
                for (Long goodsId : ids) {
                    solrManagerService.addItemToSolr(goodsId);
                }
            }
            return new Result(true, "状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "状态修改失败!");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long goodsId : ids) {
                    //1. 到数据库中根据商品id, 逻辑删除商品数据
                    goodsService.delete(goodsId);
                    //2.根据商品id删除solr索引库中的库存数据
                  //  solrManagerService.deleteItemByGoodsId(goodsId);
                }
            }
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }


}
