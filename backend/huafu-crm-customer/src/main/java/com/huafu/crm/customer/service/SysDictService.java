package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huafu.crm.common.entity.SysDictItem;
import com.huafu.crm.common.entity.SysDictType;
import java.util.List;
import java.util.Map;

public interface SysDictService extends IService<SysDictType> {
    List<SysDictType> getAllDictTypes();
    List<SysDictItem> getDictItemsByDictCode(String dictCode);
    List<SysDictItem> getDictItemsByDictId(Long dictId);
    Map<String, List<SysDictItem>> getBatchDictItems(List<String> dictCodes);
    boolean saveDictType(SysDictType dictType);
    boolean updateDictType(SysDictType dictType);
    boolean deleteDictType(Long id);
    boolean saveDictItem(SysDictItem dictItem);
    boolean updateDictItem(SysDictItem dictItem);
    boolean deleteDictItem(Long id);
    boolean updateDictItemStatus(Long id, Integer status);
}
