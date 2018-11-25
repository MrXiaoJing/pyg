package com.pyg.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
//    搜索
    public Map<String,Object> search(Map searchMap);
    public void impoetList(List list);
    public void deleteByGoodsIds(Long[] goodsIdList);
}
