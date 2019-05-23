package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;


@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    SpecificationOptionDao specOptionDao;
    @Autowired
    TypeTemplateDao templateDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows) {
        //将品牌和规格选项缓存到redis中
        List<TypeTemplate> typeTemplates = templateDao.selectByExample(null);
        if (typeTemplates!=null){
            for (TypeTemplate template : typeTemplates) {
                String brandJsonStr = template.getBrandIds();
                //将json转换成集合
                List<Map> brandList = JSON.parseArray(brandJsonStr, Map.class);
                //将模板id作为小key,品牌集合作为value缓存到redis中
                redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).put(template.getId(), brandList);
                List<Map> specList = findBySpecList(template.getId());

            }

        }
        //
        //开启分页助手
        PageHelper.startPage(page,rows);
        //创建查询对象
        TypeTemplateQuery query = new TypeTemplateQuery();
        //创建sql语句查询对象
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        if (typeTemplate!=null){
            if (typeTemplate.getName()!=null&&"".equals(typeTemplate.getName())){
                criteria.andNameLike("%"+typeTemplate.getName()+"%");
            }
        }


       Page<TypeTemplate> templates= ( Page<TypeTemplate>)templateDao.selectByExample(query);


        return new PageResult(templates.getTotal(),templates.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        templateDao.insertSelective(typeTemplate);
    }
//修改之数据回显
    @Override
    public TypeTemplate findOne(Long id) {
        TypeTemplate typeTemplate = templateDao.selectByPrimaryKey(id);
        return typeTemplate;
    }
//修改
    @Override
    public void update(TypeTemplate typeTemplate) {
        templateDao.updateByPrimaryKeySelective(typeTemplate);
    }
//删除
    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {

                templateDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //查询模板对象
        TypeTemplate typeTemplate = templateDao.selectByPrimaryKey(id);
        //获取json字符串
        String specMap = typeTemplate.getSpecIds();
        //解析字符串为java 对象
        List<Map> specList = JSON.parseArray(specMap, Map.class);
        if (specList!=null){
            for (Map map : specList) {
                long specId = Long.parseLong(String.valueOf(map.get("id")));
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                List<SpecificationOption> optionList = specOptionDao.selectByExample(specificationOptionQuery);
                map.put("options",optionList);

            }
        }


        return specList;
    }

}
