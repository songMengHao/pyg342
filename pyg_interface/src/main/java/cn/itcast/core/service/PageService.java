package cn.itcast.core.service;

import java.util.Map;

public interface PageService {
    /**
     * 根据商品id,获取生成静态页面需要的所有数据
     * @param goodsId
     * @return
     */
    public Map<String,Object> findGoodsData(Long goodsId);
    /*
    * 生成静态化页面
    * */
    public void createStatic(Long goodsId,Map<String,Object> rootMap) throws Exception;
}
