package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;


import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);
        //删除Redis原来已有的数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
    }

    @Override
    public void edit(Content content) {
        //根据广告id查询数据库中的广告对象
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        //从redis中删除原有的id
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(oldContent.getCategoryId());
        //根据新的id从Redis中删除广告集
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Override
    public void delAll(Long[] ids) {
        if(ids != null){
            for(Long id : ids){
                //根据广告id到数据库中查询广告对象
                Content content = findOne(id);
                //根据对象删除redis中的对应的广告集数据
                redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content
                        .getCategoryId());
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }
//根据广告分类id查询广告列表
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        //创建查询对象
        ContentQuery query = new ContentQuery();
        //创建sql语句查询对象
        ContentQuery.Criteria criteria = query.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        //开启状态
        criteria.andStatusEqualTo("1");
        //升序排列
        query.setOrderByClause("sort_order desc");
        List<Content> contentList = contentDao.selectByExample(query);


        return contentList;
    }
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {
        //从Redis中获取广告数据列表
        List<Content> contentList = (List<Content>)redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).get
                (categoryId);
        //如果没有则从数据库中获取,并传递给redis
        if (contentList==null||contentList.size()==0){
           contentList= findByCategoryId(categoryId);
           redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).put(categoryId,contentList);

        }
        return contentList;
    }

}