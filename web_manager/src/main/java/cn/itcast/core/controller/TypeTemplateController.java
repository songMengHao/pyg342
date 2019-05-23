package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.zookeeper.data.Id;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    TypeTemplateService typeTemplateService;
    @RequestMapping("/search")
    public PageResult findPage(@RequestBody(required = false) TypeTemplate typeTemplate, Integer
            page, Integer
            rows){
    PageResult pageResult= typeTemplateService.findPage(typeTemplate,page,rows);
    return pageResult;

    }
    //添加
    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true,"添加成功了呢");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
    //修改之数据回显
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){
       TypeTemplate typeTemplate= typeTemplateService.findOne(id);
       return typeTemplate;
    }
    //修改
    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true,"修改成功了呢");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败了呢");
        }

    }
    //删除
    @RequestMapping("/delete")
    public Result delete(Long [] ids){
        try {
            typeTemplateService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }

    }


}
