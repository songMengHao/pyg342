package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;

    @RequestMapping("/search")
    public PageResult findPage(Integer page, Integer rows, @RequestBody Seller seller) {
        PageResult pageResult = sellerService.findPage(page, rows, seller);
        return pageResult;
    }

    //修改之页面回显
    @RequestMapping("/findOne")
    public Seller findOne(String id) {
        Seller seller = sellerService.findOne(id);
        return seller;

    }

    //修改状态
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId, String status) {

        try {


            sellerService.updateStatus(sellerId, status);
            return  new Result(true,"审核成功");



        } catch (Exception e) {

            e.printStackTrace();

            return new Result(false, "审核失败");
        }
    }
}
