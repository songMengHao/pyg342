package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.data.web.config.QuerydslWebConfiguration;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {


    @Autowired
    SolrTemplate solrTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        //获取用户选中的分类条件
        String categoryName = String.valueOf(searchMap.get("category"));
        //高亮高亮, 分页, 过滤, 排序查询
        Map<String, Object> resultMap = highPageQuery(searchMap);

        //  根据关键字查询,对分类进行分组查询
        List<String> groupByCategory = findGroupByCategory(searchMap);
        resultMap.put("categoryList", groupByCategory);
        if (categoryName != null && !"".equals(categoryName)) {
            //根据用户选中的分类, 找对应的品牌集合和规格集合作为过滤条件
            Map<String, List> brandAndSpecList = findBrandListAndSpecListByCategoryName(categoryName);
            resultMap.putAll(brandAndSpecList);
        } else {
            if (groupByCategory != null && groupByCategory.size() > 0) {
                //如果用户没有选中具体分类, 则默认根据查询得到的分类集合中的第一个分类名称,
                // 找到对应的品牌集合和规格集合作为过滤条件
                categoryName = groupByCategory.get(0);
                Map<String, List> brandAndSpecList = findBrandListAndSpecListByCategoryName(categoryName);
                resultMap.putAll(brandAndSpecList);
            }
        }

        return resultMap;
    }

    //高亮高亮, 分页, 过滤, 排序查询
    private Map<String, Object> highPageQuery(Map searchMap) {
        //查询关键字
        String keywords = String.valueOf(searchMap.get("keywords"));
        //当前页
        Integer pageNo = Integer.parseInt(String.valueOf(searchMap.get("pageNo")));
        //每页显示条数
        Integer pageSize = Integer.parseInt(String.valueOf(searchMap.get("pageSize")));
        //获取排序的域名
        String sortField = String.valueOf(searchMap.get("sortField"));
        //获取排序方式
        String sortType = String.valueOf(searchMap.get("sort"));
        //获取选中的分类名称
        String category = String.valueOf(searchMap.get("category"));
        //获取选中的品牌
        String brand = String.valueOf(searchMap.get("brand"));
        //获取选中的规格
        String spec = String.valueOf(searchMap.get("spec"));
        //获取选中的价格
        String price = String.valueOf(searchMap.get("price"));
        /**
         * 创建查询对象
         */
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        /**
         * 设置查询条件
         */
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        /**
         * 设置分页条件
         */
        if (pageNo == null || "".equals(pageNo) || pageNo < 1) {
            pageNo = 1;
        }

        //计算从第几条开始查询
        Integer start = (pageNo - 1) * pageSize;

        //从第几条开始查询
        query.setOffset(start);
        //每页查询多少条数据
        query.setRows(pageSize);
        //高亮查询
        //创建高亮选项
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置需要高亮显示的域名
        highlightOptions.addField("item_title");
        //设置前缀
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        //设置后缀
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);
        /**
         * 排序
         */
        if (sortField != null && sortType != null && !"".equals(sortField) && !"".equals(sortType)) {
            //升序
            if ("ASC".equals(sortType)) {
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //将排序对象放到查询对象中
                query.addSort(sort);
            }
            //降序
            if ("DESC".equals(sortType)) {
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                //将排序对象放到查询对象中
                query.addSort(sort);
            }

        }
        /**
         * 过滤查询
         */
        //根据选中的分类过滤
        if (category != null && !"".equals(category)) {
            //创建过滤查询对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            //创建过滤条件对象
            Criteria filterCriteria = new Criteria("item_category").is(category);
            //将过滤条件放入过滤查询对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤查询对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据选中的品牌过滤
        if (brand != null && !"".equals(brand)) {
            //创建过滤查询对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            //创建过滤条件对象
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            //将过滤条件放入过滤查询对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤查询对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据选中的规格选项过滤
        if (spec != null && !"".equals(spec)) {
            //将选中的多个规格json字符串转换成Map
            Map map = JSON.parseObject(spec, Map.class);
            //因为无法对map遍历所以我们将它里面的键值对集合变为一个对象集合
            if (map != null && map.size() > 0) {
                Set<Map.Entry<String, String>> entries = map.entrySet();
                //遍历对象集合
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建过滤条件对象
                    Criteria filterCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                    //将过滤条件放入过滤查询对象中
                    filterQuery.addCriteria(filterCriteria);
                    //将过滤查询对象放入查询对象中
                    query.addFilterQuery(filterQuery);
                }
            }

        }
        //根据选中价格过滤
        if (price!=null && !"".equals(price)){
            //分割
            String[] priceArray = price.split("-");
            //如果最小值大于零第一个元素大于等于最小值
            if (!"0".equals(priceArray[0])){
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual
                        (priceArray[0]);
                simpleFilterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
            //如果最大值小于* 第一个元素小于最大值
            if (!"*".equals(priceArray[1])){
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceArray[1]);
                simpleFilterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(simpleFilterQuery);

            }
        }



        /**
         * 查询并返回结果
         */
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);

        //获取高亮结果集
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        List<Item> resultList = new ArrayList<>();
        if (highlighted != null) {
            for (HighlightEntry<Item> highlightEntry : highlighted) {
                //不带高亮标题的实体对象
                Item entity = highlightEntry.getEntity();
                //带高亮标题的实体对象
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                if (highlightList != null && highlightList.size() > 0) {
                    //获得高亮标题集合
                    List<String> snipplets = highlightList.get(0).getSnipplets();
                    if (snipplets != null && snipplets.size() > 0) {
                        //获得高亮标题
                        String s = snipplets.get(0);
                        entity.setTitle(s);
                    }

                }
                //如果为空,则添加实体
                resultList.add(entity);

            }

        }
        /**
         * 获取查询结果中的数据封装并返回
         */
        HashMap<String, Object> resultMap = new HashMap<>();
        //封装结果集
        resultMap.put("rows", resultList);
        resultMap.put("total", items.getTotalElements());
        resultMap.put("totalPages", items.getTotalPages());
        return resultMap;
    }

    /**
     * 根据关键字查询,对分类进行分组查询,获得去重后的集合
     *
     * @param paramMap
     * @return
     */

    private List<String> findGroupByCategory(Map paramMap) {
        /**
         * 获取查询条件
         **/
        //查询
        String keywords = String.valueOf(paramMap.get("keywords"));
        //去除空格
        if (keywords != null) {
            keywords.replaceAll(" ", "");
        }
        /**
         * 创建查询对象
         */
        Query query = new SimpleQuery();
        /**
         *   设置查询条件
         */
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //将条件放入到查询对象中
        query.addCriteria(criteria);
        /**
         *  设置分组内容
         */
        //创建分组选项对象
        GroupOptions groupOptions = new GroupOptions();
        //设置根据分类域进行分组
        groupOptions.addGroupByField("item_category");
        //将分组选项放入查询对象中
        query.setGroupOptions(groupOptions);
        /**
         * 获得查询结果
         */
        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);
        /*
         * 封装结果集
         *
         * */
        List<String> resultList = new ArrayList<>();
        //获取根据分类域进行分组的结果
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntryPage = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntryPage.getContent();
        if (content != null) {
            for (GroupEntry<Item> groupEntry : content) {
                resultList.add(groupEntry.getGroupValue());
            }
        }
        return resultList;


    }

    private Map<String, List> findBrandListAndSpecListByCategoryName(String categoryName) {
        //根据分类名称到redis中寻找模板id
        Long templateId = (Long) redisTemplate.boundHashOps(Constants.REDIS_CATEGORYLIST).get
                (categoryName);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).get(templateId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).get(templateId);
        //封装数据
        HashMap<String, List> resultMap = new HashMap<>();
        resultMap.put("brandList", brandList);
        resultMap.put("specList", specList);
        return resultMap;

    }

//    /*@Override
//    public Map<String, Object> search(Map searchMap) {
//*//**
// * 获取查询条件参数
// * <p>
// * 创建查询对象
// * <p>
// * 设置查询条件
// * <p>
// * 设置分页条件
// * <p>
// * 查询返回结果
// * <p>
// * 获取查询结果中的数据封装并返回
// * <p>
// * 创建查询对象
// * <p>
// * 设置查询条件
// * <p>
// * 设置分页条件
// * <p>
// * 查询返回结果
// * <p>
// * 获取查询结果中的数据封装并返回
// *//*
//        //查询关键字
//        String keywords=String.valueOf(searchMap.get("keywords"));
//        //当前页
//        Integer pageNo=Integer.parseInt(String.valueOf(searchMap.get("pageNo")));
//        //每页显示条数
//        Integer pageSize=Integer.parseInt(String.valueOf(searchMap.get("pageSize")));
//
//        *//**
// * 创建查询对象
// *//*
//        SimpleQuery query = new SimpleQuery();
//        *//**
// * 设置查询条件
// *//*
//        Criteria criteria = new Criteria("item_keywords").is(keywords);
//        query.addCriteria(criteria);
//        *//**
// * 设置分页条件
// *//*
//        if (pageNo==null||"".equals(pageNo)||pageNo<1){
//            pageNo=1;
//        }
//        //计算从第几条开始查询
//       Integer start=(pageNo-1)*pageSize;
//        //从第几条开始查询
//        query.setOffset(start);
//        //每页查询条数
//        query.setRows(pageSize);
//        *//**
// * 查询返回结果
// *//*
//        ScoredPage<Item> items = solrTemplate.queryForPage(query, Item.class);
//
//        *//**
// * 获取查询结果中的数据封装并返回
// *//*
//        HashMap<String , Object> resultMap = new HashMap<>();
//        //封装结果集
//        resultMap.put("rows",items.getContent());
//        resultMap.put("total",items.getTotalElements());
//        resultMap.put("totalPages",items.getTotalPages());
//        return resultMap;
//    }*/


}