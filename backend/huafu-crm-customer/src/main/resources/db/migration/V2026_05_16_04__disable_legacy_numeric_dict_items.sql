-- Numeric customer fields should expose one active option set in dictionary management.
-- Legacy string enum codes came from earlier frontend-only mappings and conflict visually
-- with the integer values accepted by the current backend DTOs.
UPDATE sys_dict_item i
SET status = 0,
    deleted = 1,
    updated_at = CURRENT_TIMESTAMP
FROM sys_dict_type t
WHERE i.dict_id = t.id
  AND t.dict_code IN ('customer_type', 'customer_status', 'customer_level', 'biz_type')
  AND i.item_code IN (
      'ENTITY', 'TRADE', 'END_BRAND',
      'POTENTIAL', 'ACTIVE', 'INACTIVE', 'LOST',
      'A', 'B', 'C', 'D',
      'DOMESTIC', 'EXPORT', 'ENTRADE'
  );
