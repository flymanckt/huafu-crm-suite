package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.CustomerCreateDTO;
import com.huafu.crm.customer.entity.Customer;
import com.huafu.crm.customer.mapper.CustomerMapper;
import com.huafu.crm.customer.query.CustomerQuery;
import com.huafu.crm.customer.vo.CustomerVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
 @Mock
 private CustomerMapper mapper;

 @InjectMocks
 private CustomerServiceImpl service;

 @Test
 void createShouldInsertAndReturnVo() {
  when(mapper.insert(any(Customer.class))).thenAnswer(invocation -> {
   Customer c = invocation.getArgument(0);
   c.setId(1001L);
   return 1;
  });
  when(mapper.selectById(1001L)).thenAnswer(invocation -> {
   Customer c = new Customer();
   c.setId(1001L);
   c.setCustomerCode("HF-001");
   c.setCustomerName("华孚客户");
   c.setCustomerShortName("华孚");
   c.setDistrict("西湖区");
   c.setBundleCustomerName("捆绑客户");
   c.setBundleBrand("捆绑品牌");
   c.setStatus(6);
   return c;
  });

	  CustomerCreateDTO dto = new CustomerCreateDTO("HF-001", "华孚客户", "华孚",
	   1, 2, 6, "浙江", "杭州", "西湖区", "某路1号",
	   "张三", "13800000000", "采购",
	   new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("0.13"),
	   30, "SAP-001", null, 1L,
	   null, null, null, null, null, null, null, null, null, null,
	   "捆绑客户", "捆绑品牌",
	   null, null, null, null, null, null, null, null, null, null,
	   null, null, null, null, null, null, null, null, null, null,
	   null, null, null, null, null, null, null, null);

  CustomerVO vo = service.create(dto);

  ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
  verify(mapper).insert(captor.capture());
  Customer saved = captor.getValue();
  assertThat(saved.getCustomerCode()).isEqualTo("HF-001");
  assertThat(saved.getCustomerName()).isEqualTo("华孚客户");
  assertThat(saved.getDistrict()).isEqualTo("西湖区");
  assertThat(saved.getBundleCustomerName()).isEqualTo("捆绑客户");
  assertThat(saved.getBundleBrand()).isEqualTo("捆绑品牌");
  assertThat(saved.getStatus()).isEqualTo(6);
 }

 @Test
 void getShouldReturnVoWhenCustomerExists() {
  Customer c = buildCustomer(10L, "客户A");
  when(mapper.selectById(10L)).thenReturn(c);

  CustomerVO vo = service.get(10L);

  assertThat(vo.id()).isEqualTo(10L);
  assertThat(vo.customerName()).isEqualTo("客户A");
 }

 @Test
 void getShouldThrowBizExceptionWhenMissing() {
  when(mapper.selectById(404L)).thenReturn(null);

  assertThatThrownBy(() -> service.get(404L))
   .isInstanceOf(BizException.class)
   .hasMessage("客户不存在");
 }

 @Test
 @SuppressWarnings({"unchecked", "rawtypes"})
 void pageShouldDelegateToMapperAndMapRecords() {
  Page<Customer> mapperPage = new Page<>(1, 20);
  mapperPage.setTotal(1);
  mapperPage.setRecords(List.of(buildCustomer(20L, "分页客户")));
  when(mapper.selectPage(any(Page.class), any())).thenReturn(mapperPage);

  CustomerQuery query = new CustomerQuery(1, 20, null, null, null, null, null, null, null, null, null, null, null, null, null);
  PageResult<CustomerVO> result = service.page(query);

  assertThat(result.current()).isEqualTo(1);
  assertThat(result.size()).isEqualTo(20);
  assertThat(result.total()).isEqualTo(1);
  assertThat(result.records()).hasSize(1);
  assertThat(result.records().get(0).customerName()).isEqualTo("分页客户");
 }

 @Test
 void deleteShouldDelegateToMapper() {
  service.delete(11L);
  verify(mapper).deleteById(11L);
 }

 @Test
 void claimFromPoolShouldSetOwnerAndClearPoolTime() {
  Customer c = buildCustomer(12L, "公海客户");
  c.setOwnerUserId(null);
  c.setPublicPoolTime(java.time.OffsetDateTime.now());
  when(mapper.selectById(12L)).thenReturn(c);

  service.claimFromPool(12L);

  ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
  verify(mapper).updateById(captor.capture());
  assertThat(captor.getValue().getOwnerUserId()).isNotNull();
  assertThat(captor.getValue().getPublicPoolTime()).isNull();
 }

 private Customer buildCustomer(Long id, String name) {
  Customer c = new Customer();
  c.setId(id);
  c.setCustomerCode("C-" + id);
  c.setCustomerName(name);
  c.setCustomerShortName(name);
  c.setType(1);
  c.setLevel(2);
  c.setStatus(1);
  c.setMainContactName("联系人");
  c.setMainContactPhone("13900000000");
  c.setOwnerUserId(1L);
  c.setDeleted(0);
  return c;
 }
}
