package cn.itcast.core.service;


import cn.itcast.core.dao.good.BrandDao;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }


    @Override
    public PageResult findPage(Brand brand, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        //创建查询对象
        BrandQuery brandQuery = new BrandQuery();
        //按照id降序
        brandQuery.setOrderByClause("id desc");
        //创建sql语句条件查询对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand != null) {
            if (brand.getName() != null && !"".equals(brand.getName())) {
                criteria.andNameLike("%" + brand.getName() + "%");
            }
            if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar())) {
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }


        // 查询并返回结果
        Page<Brand> brandList = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(brandList.getTotal(), brandList.getResult());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);

    }

    @Override
    public Brand findOne(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }


    }

    @Override
    public List<Map> selectOptionList() {

        return brandDao.selectOptionList();
    }
}
