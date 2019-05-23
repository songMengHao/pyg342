package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.springframework.beans.factory.annotation.Autowired;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerDao sellerDao;
    @Override
    public void add(Seller seller) {
        seller.setStatus("0");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format1 = format.format(date);
        Date parse = null;
        try {
            parse = format.parse(format1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        seller.setCreateTime(parse);
        sellerDao.insertSelective(seller);
    }

    @Override
    public List<Seller> findAll() {
        return sellerDao.selectByExample(null);
    }
//查找分页
    @Override
    public PageResult findPage(Integer page, Integer rows,Seller seller) {
        //开启分页助手
        PageHelper.startPage(page,rows);
        //创建查询对象
        SellerQuery sellerQuery = new SellerQuery();
        //创建sql语句查询对象
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if (seller.getName()!=null&&!"".equals(seller.getName())){
            criteria.andNameLike("%"+seller.getName()+"%");
        }
        if (seller.getStatus()!=null&&!"".equals(seller.getStatus())){
            criteria.andStatusEqualTo(seller.getStatus());
        }
        Page<Seller> sellerList = (Page<Seller>)sellerDao.selectByExample(sellerQuery);


        return new PageResult(sellerList.getTotal(),sellerList.getResult());
    }

    @Override
    public Seller findOne(String id) {
        Seller seller = sellerDao.selectByPrimaryKey(id);
        return seller;
    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

}
