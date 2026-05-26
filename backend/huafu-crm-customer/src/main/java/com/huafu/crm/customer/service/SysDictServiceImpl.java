package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huafu.crm.common.entity.SysDictItem;
import com.huafu.crm.common.entity.SysDictType;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.mapper.SysDictItemMapper;
import com.huafu.crm.common.mapper.SysDictTypeMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictService {

    private final SysDictItemMapper dictItemMapper;

    public SysDictServiceImpl(SysDictItemMapper dictItemMapper) {
        this.dictItemMapper = dictItemMapper;
    }

    @Override
    public List<SysDictType> getAllDictTypes() {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getStatus, 1)
               .eq(SysDictType::getDeleted, 0)
               .orderByAsc(SysDictType::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public List<SysDictItem> getDictItemsByDictCode(String dictCode) {
        LambdaQueryWrapper<SysDictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(SysDictType::getDictCode, dictCode)
                   .eq(SysDictType::getDeleted, 0);
        SysDictType dictType = this.getOne(typeWrapper);
        if (dictType == null) {
            return Collections.emptyList();
        }
        return getDictItemsByDictId(dictType.getId());
    }

    @Override
    public List<SysDictItem> getDictItemsByDictId(Long dictId) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, dictId)
               .eq(SysDictItem::getStatus, 1)
               .eq(SysDictItem::getDeleted, 0)
               .orderByAsc(SysDictItem::getSortOrder);
        return dictItemMapper.selectList(wrapper);
    }

    @Override
    public Map<String, List<SysDictItem>> getBatchDictItems(List<String> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        
        LambdaQueryWrapper<SysDictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.in(SysDictType::getDictCode, dictCodes)
                   .eq(SysDictType::getStatus, 1)
                   .eq(SysDictType::getDeleted, 0);
        List<SysDictType> dictTypes = this.list(typeWrapper);
        
        if (dictTypes.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<Long> dictIds = dictTypes.stream().map(SysDictType::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<SysDictItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(SysDictItem::getDictId, dictIds)
                   .eq(SysDictItem::getStatus, 1)
                   .eq(SysDictItem::getDeleted, 0)
                   .orderByAsc(SysDictItem::getSortOrder);
        List<SysDictItem> allItems = dictItemMapper.selectList(itemWrapper);
        
        Map<Long, String> dictIdToCode = dictTypes.stream()
                .collect(Collectors.toMap(SysDictType::getId, SysDictType::getDictCode));
        
        Map<String, List<SysDictItem>> result = new LinkedHashMap<>();
        for (String code : dictCodes) {
            result.put(code, new ArrayList<>());
        }
        
        for (SysDictItem item : allItems) {
            String code = dictIdToCode.get(item.getDictId());
            if (code != null && result.containsKey(code)) {
                result.get(code).add(item);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean saveDictType(SysDictType dictType) {
        if (StringUtils.hasText(dictType.getDictCode())) {
            LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictType::getDictCode, dictType.getDictCode());
            if (this.count(wrapper) > 0) {
                throw new RuntimeException("字典编码已存在");
            }
        }
        return this.save(dictType);
    }

    @Override
    @Transactional
    public boolean updateDictType(SysDictType dictType) {
        return this.updateById(dictType);
    }

    @Override
    @Transactional
    public boolean deleteDictType(Long id) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, id);
        long count = dictItemMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该字典类型下存在字典项，无法删除");
        }
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean saveDictItem(SysDictItem dictItem) {
        prepareAndValidateDictItem(dictItem, true);
        return dictItemMapper.insert(dictItem) > 0;
    }

    @Override
    @Transactional
    public boolean updateDictItem(SysDictItem dictItem) {
        prepareAndValidateDictItem(dictItem, false);
        return dictItemMapper.updateById(dictItem) > 0;
    }

    private void prepareAndValidateDictItem(SysDictItem dictItem, boolean create) {
        if (dictItem == null) {
            throw new BizException(1001, "字典项不能为空");
        }
        if (dictItem.getDictId() == null) {
            throw new BizException(1001, "字典类型不能为空");
        }
        if (!StringUtils.hasText(dictItem.getItemCode())) {
            throw new BizException(1001, "字典项编码不能为空");
        }
        if (!StringUtils.hasText(dictItem.getItemName())) {
            throw new BizException(1001, "字典项名称不能为空");
        }

        dictItem.setItemCode(dictItem.getItemCode().trim());
        dictItem.setItemName(dictItem.getItemName().trim());
        if (StringUtils.hasText(dictItem.getItemValue())) {
            dictItem.setItemValue(dictItem.getItemValue().trim());
        } else {
            dictItem.setItemValue(dictItem.getItemCode());
        }
        if (dictItem.getSortOrder() == null) dictItem.setSortOrder(0);
        if (dictItem.getStatus() == null) dictItem.setStatus(1);
        if (dictItem.getDefaultFlag() == null) dictItem.setDefaultFlag(0);
        if (dictItem.getShowCode() == null) dictItem.setShowCode(1);
        if (create) dictItem.setDeleted(0);

        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, dictItem.getDictId())
               .eq(SysDictItem::getItemCode, dictItem.getItemCode())
               .eq(SysDictItem::getDeleted, 0);
        if (dictItem.getId() != null) {
            wrapper.ne(SysDictItem::getId, dictItem.getId());
        }
        if (dictItemMapper.selectCount(wrapper) > 0) {
            throw new BizException(1002, "字典项编码已存在，请更换编码：" + dictItem.getItemCode());
        }
    }

    @Override
    @Transactional
    public boolean deleteDictItem(Long id) {
        return dictItemMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean updateDictItemStatus(Long id, Integer status) {
        SysDictItem item = new SysDictItem();
        item.setId(id);
        item.setStatus(status);
        return dictItemMapper.updateById(item) > 0;
    }
}
