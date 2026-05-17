package com.huafu.crm.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new Phase0TenantLineHandler()));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /** Phase 0 placeholder: real tenant id should come from TenantContext after auth middleware lands. */
    static class Phase0TenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {
        @Override public Expression getTenantId() { return new LongValue(1L); }
        @Override public String getTenantIdColumn() { return "tenant_id"; }
        @Override public boolean ignoreTable(String tableName) {
            return tableName.startsWith("sys_")
                || tableName.equals("crm_customer_ext")
                || tableName.equals("crm_customer_overview")
                || tableName.equals("crm_customer_yarn_usage")
                || tableName.equals("crm_customer_sap_org")
                || tableName.equals("crm_customer_sap_partner")
                || tableName.equals("crm_customer_attachment")
                || tableName.equals("crm_customer_contact")
                || tableName.equals("crm_customer_bundle")
                || tableName.equals("sys_dict_type")
                || tableName.equals("sys_dict_item")
                || tableName.equals("sys_user_column_config")
                || tableName.equals("sys_user_filter_config");
        }
    }
}
