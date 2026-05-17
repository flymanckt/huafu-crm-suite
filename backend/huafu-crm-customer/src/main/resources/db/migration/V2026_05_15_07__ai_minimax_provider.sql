-- V2026.05.15.07: MiniMax 供应商预设兼容
-- 2026-05-15

UPDATE sys_config
SET config_value = 'MINIMAX',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'ai.provider'
  AND EXISTS (
      SELECT 1
      FROM sys_config base
      WHERE base.config_key = 'ai.base_url'
        AND base.deleted = 0
        AND LOWER(COALESCE(base.config_value, '')) LIKE '%minimax%'
  );
