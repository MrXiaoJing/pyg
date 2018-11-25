package com.pyg.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbTypeTemplateMapper;
import com.pyg.pojo.TbTypeTemplate;
import com.pyg.pojo.TbTypeTemplateExample;
import com.pyg.pojo.TbTypeTemplateExample.Criteria;
import com.pyg.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	//将数据存入缓存
	private void saveToRedis(){
		//获取模板数据
		List<TbTypeTemplate> typeTemplateList = findAll();
		//循环模板
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			//存储品牌列表
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
			//存储规格列表
			List<Map> specList = findSpecList(typeTemplate.getId()); //根据模板ID查询规格列表
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
		}
		System.out.println("缓存模板数据");
	}
	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		saveToRedis();//存入数据到缓存
		PageHelper.startPage(pageNum, pageSize);
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecList(Long id) {
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		List<Map> list = JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);
		for (Map map : list) {
			TbSpecificationOptionExample example =new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(new Long ((int)map.get("id")));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
		map.put("options",options);
		}
		return list;
	}

}
