package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.net.httpserver.Authenticator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

//@RestController 表示这个类的所有方法的返回形式为json格式字符串返回
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<Brand> findAll() {
        List<Brand> list = brandService.findAll();
        return list;
    }



 /*   @RequestMapping("/findPage")
    public PageResult findPage(Integer page,Integer rows){
        PageResult pageResult = brandService.findPage(page, rows);
        return pageResult;
    }*/
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"保存成功");
        } catch (Exception e) {

            e.printStackTrace();
            return new Result(false,"保存失败");
        }

    }
    @RequestMapping("/findOne")
    public Brand findOne(Long id){
       Brand brand= brandService.findOne(id);
        return brand;
    }
    @RequestMapping("/update")
    public Result update(@RequestBody Brand  brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }
    @RequestMapping("/delete")
    public  Result delete(Long [] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除 失败");
        }
    }
    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand,Integer page,Integer rows){
        PageResult pageResult = brandService.findPage(brand, page, rows);
        return pageResult;
    }
    //下拉框
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        List<Map> maps = brandService.selectOptionList();
        return maps;
    }


}
