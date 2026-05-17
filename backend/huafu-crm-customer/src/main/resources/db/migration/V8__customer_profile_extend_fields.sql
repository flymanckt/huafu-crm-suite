-- V8: BUG-002 修复 - crm_customer_profile 新增 tags/remark/customerStage 字段
-- 2026-05-08
-- 可重复执行

ALTER TABLE crm_customer_profile ADD COLUMN IF NOT EXISTS tags TEXT;
ALTER TABLE crm_customer_profile ADD COLUMN IF NOT EXISTS remark TEXT;
ALTER TABLE crm_customer_profile ADD COLUMN IF NOT EXISTS customer_stage VARCHAR(64);
