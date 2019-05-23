package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<Brand> findAll();

    PageResult findPage(Brand brand, Integer page, Integer rows);

    void add(Brand brand);


    void update(Brand brand);

    Brand findOne(Long id);

    void delete(Long[] ids);

    List<Map> selectOptionList();
}
