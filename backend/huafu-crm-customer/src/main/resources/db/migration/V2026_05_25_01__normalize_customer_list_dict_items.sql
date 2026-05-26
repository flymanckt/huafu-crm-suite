-- Normalize customer list dictionaries so list/search/batch UI uses the current master-data vocabulary.
WITH target_types AS (
    SELECT id, dict_code
    FROM sys_dict_type
    WHERE dict_code IN (
        'customer_type',
        'customer_status',
        'biz_type',
        'customer_category',
        'customer_source',
        'customer_group'
    )
)
UPDATE sys_dict_item i
SET status = CASE
        WHEN t.dict_code = 'customer_type' AND i.item_code IN ('1', '2') THEN 1
        WHEN t.dict_code = 'customer_status' AND i.item_code IN ('1', '2') THEN 1
        WHEN t.dict_code = 'biz_type' AND i.item_code IN ('1', '2', '3') THEN 1
        WHEN t.dict_code = 'customer_category' AND i.item_code IN (
            '0001', '0002', '0003', '0004', '0005', '0006', '0007',
            '0008', '0009', '0010', '0011', '0012', '0013'
        ) THEN 1
        WHEN t.dict_code = 'customer_source' AND i.item_code IN ('1', '2', '3', '4') THEN 1
        WHEN t.dict_code = 'customer_group' AND i.item_code IN ('1', '2', '3') THEN 1
        ELSE 0
    END,
    show_code = 0,
    updated_at = CURRENT_TIMESTAMP
FROM target_types t
WHERE i.dict_id = t.id
  AND i.deleted = 0;
