package com.pyg.sellergoods.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //需求：查找所有品牌信息
    public List<TbBrand> findAll();
    //需求：分页查找
    public PageResult findPage(int pageNum,int pageSize);
    //需求：添加
    public void add(TbBrand tbBrand);
    //单个查询品牌
    public TbBrand findOne(Long id);
    //需求：修改
    public void update(TbBrand tbBrand);
    //需求：删除
    public void delete(Long[] ids);
    //需求：按条件查询
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
    public List<Map> selectOptionList();
}
