-- Allow a dictionary item code to be reused after the old item has been logically deleted.
ALTER TABLE sys_dict_item DROP CONSTRAINT IF EXISTS uk_dict_id_item_code;

CREATE UNIQUE INDEX IF NOT EXISTS uk_dict_item_active_code
    ON sys_dict_item (dict_id, item_code)
    WHERE deleted = 0;
