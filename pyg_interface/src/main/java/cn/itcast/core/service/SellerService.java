package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

import java.util.List;

public interface SellerService {

    void add(Seller seller);
    //显示所有
    List<Seller> findAll();

    PageResult findPage( Integer page, Integer rows,Seller seller);

    Seller findOne(String id);

    void updateStatus(String sellerId, String status);
}
