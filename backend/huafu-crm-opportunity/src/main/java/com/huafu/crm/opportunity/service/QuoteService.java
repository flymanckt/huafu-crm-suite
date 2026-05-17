package com.huafu.crm.opportunity.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.QuoteCreateDTO;
import com.huafu.crm.opportunity.dto.QuoteItemDTO;
import com.huafu.crm.opportunity.dto.QuoteUpdateDTO;
import com.huafu.crm.opportunity.vo.QuoteVO;
import com.huafu.crm.opportunity.vo.QuoteItemVO;
import java.util.List;

public interface QuoteService {
    QuoteVO create(QuoteCreateDTO dto);
    QuoteVO getById(Long id);
    PageResult<QuoteVO> page(Long customerId, Long salesUserId, Integer status, long current, long size);
    QuoteVO update(Long id, QuoteUpdateDTO dto);
    void delete(Long id);
    QuoteVO send(Long id);
    QuoteVO confirm(Long id);
    QuoteItemVO addItem(Long quoteId, QuoteItemDTO dto);
    QuoteItemVO updateItem(Long itemId, QuoteItemDTO dto);
    void deleteItem(Long itemId);
    List<QuoteItemVO> getItems(Long quoteId);
}
