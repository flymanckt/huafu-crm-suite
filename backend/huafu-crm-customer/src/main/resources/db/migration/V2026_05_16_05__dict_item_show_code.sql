ALTER TABLE sys_dict_item
    ADD COLUMN IF NOT EXISTS show_code smallint DEFAULT 1;

COMMENT ON COLUMN sys_dict_item.show_code IS '前台显示字典项代码：1显示，0隐藏';

UPDATE sys_dict_item
SET show_code = 1
WHERE show_code IS NULL;
