package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.CredentialException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    SpecificationDao specificationDao;
    @Override
    public PageResult findPage(Specification spec, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        //创建查询对象
        SpecificationQuery query = new SpecificationQuery();
        //创建sql语句查询对象
        SpecificationQuery.Criteria criteria = query.createCriteria();
        if (spec.getSpecName()!=null&&"".equals(spec.getSpecName())){
            criteria.andSpecNameLike("%"+spec.getSpecName()+"%");
        }
        Page<Specification>specList = (Page<Specification>)specificationDao.selectByExample(query);
        return new PageResult(specList.getTotal(),specList.getResult());
    }
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    @Override
    public void add(SpecEntity specEntity) {
        //插入规格
        specificationDao.insertSelective(specEntity.getSpecification());
        //插入规格选项
        List<SpecificationOption> specificationOptionList = specEntity.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //设置规格选项的id
            specificationOption.setSpecId(specEntity.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }

    }

    @Override
    public SpecEntity findOne(Long id) {
        //根据主键id查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        //根据主键id查询选项集
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        //创建一个sql语句查询对象
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> optionList = specificationOptionDao.selectByExample(specificationOptionQuery);

        //封装实体对象
        SpecEntity specEntity = new SpecEntity();
        specEntity.setSpecification(specification);
        specEntity.setSpecificationOptionList(optionList);

        return specEntity;
    }


    @Override
    public void update(SpecEntity specEntity) {
        //保存修改的规格
        specificationDao.updateByPrimaryKey(specEntity.getSpecification());
        //删除原有的规格选项
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = optionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specEntity.getSpecification().getId());
        specificationOptionDao.deleteByExample(optionQuery);
        //循环插入数据
        List<SpecificationOption> optionList = specEntity.getSpecificationOptionList();
        for (SpecificationOption specificationOption : optionList) {
            specificationOption.setSpecId(specEntity.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }

    }
    //删除
    @Override
    public void delete(Long[] ids) {

        for (Long id : ids) {
            //根据id删除
            specificationDao.deleteByPrimaryKey(id);
            SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = optionQuery.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionDao.deleteByExample(optionQuery);

        }
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
