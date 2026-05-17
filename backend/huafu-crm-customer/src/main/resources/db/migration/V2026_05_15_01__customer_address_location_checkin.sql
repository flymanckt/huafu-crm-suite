-- V2026.05.15.01: 客户地址定位与销售打卡地址校验
-- 2026-05-15

-- 客户地址主数据：支持高德定位、行政区划回填、多地址打卡半径
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS country VARCHAR(64) DEFAULT '中国';
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS full_address VARCHAR(512);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS longitude DECIMAL(12,8);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS latitude DECIMAL(11,8);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS amap_poi_id VARCHAR(64);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS amap_poi_name VARCHAR(255);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS amap_adcode VARCHAR(32);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS amap_level VARCHAR(32);
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS location_source VARCHAR(32) DEFAULT 'MANUAL';
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS location_verified SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS location_verified_time TIMESTAMPTZ;
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS checkin_radius_meters INT DEFAULT 500;
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS address_remark VARCHAR(512);

UPDATE crm_customer_address
SET country = COALESCE(NULLIF(country, ''), '中国'),
    full_address = COALESCE(NULLIF(full_address, ''), CONCAT(COALESCE(province, ''), COALESCE(city, ''), COALESCE(district, ''), COALESCE(address_detail, ''))),
    checkin_radius_meters = COALESCE(NULLIF(checkin_radius_meters, 0), 500),
    location_source = COALESCE(NULLIF(location_source, ''), CASE WHEN longitude IS NOT NULL AND latitude IS NOT NULL THEN 'AMAP' ELSE 'MANUAL' END),
    location_verified = COALESCE(location_verified, CASE WHEN longitude IS NOT NULL AND latitude IS NOT NULL THEN 1 ELSE 0 END)
WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_customer_address_location ON crm_customer_address(customer_id, location_verified, address_type);
CREATE INDEX IF NOT EXISTS idx_customer_address_adcode ON crm_customer_address(amap_adcode);

COMMENT ON COLUMN crm_customer_address.full_address IS '高德返回或人工拼接的完整地址';
COMMENT ON COLUMN crm_customer_address.longitude IS '高德经度';
COMMENT ON COLUMN crm_customer_address.latitude IS '高德纬度';
COMMENT ON COLUMN crm_customer_address.amap_poi_id IS '高德POI ID';
COMMENT ON COLUMN crm_customer_address.amap_adcode IS '高德行政区划代码';
COMMENT ON COLUMN crm_customer_address.location_verified IS '定位是否已校验 0否 1是';
COMMENT ON COLUMN crm_customer_address.checkin_radius_meters IS '销售打卡允许偏差半径，单位米';

-- 拜访/打卡记录：记录本次打卡坐标与客户地址匹配结果
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS customer_address_id BIGINT;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_longitude DECIMAL(12,8);
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_latitude DECIMAL(11,8);
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_address VARCHAR(512);
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_time TIMESTAMPTZ;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_distance_meters DECIMAL(10,2);
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_radius_meters INT;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_matched SMALLINT DEFAULT 0;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS checkin_result VARCHAR(32);

UPDATE crm_visit_record
SET checkin_longitude = COALESCE(checkin_longitude, longitude),
    checkin_latitude = COALESCE(checkin_latitude, latitude),
    checkin_address = COALESCE(NULLIF(checkin_address, ''), location_name),
    checkin_time = COALESCE(checkin_time, created_time),
    checkin_result = COALESCE(NULLIF(checkin_result, ''), CASE WHEN longitude IS NOT NULL AND latitude IS NOT NULL THEN 'UNCHECKED' ELSE NULL END)
WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_visit_record_customer_address ON crm_visit_record(customer_id, customer_address_id);
CREATE INDEX IF NOT EXISTS idx_visit_record_checkin ON crm_visit_record(checkin_result, checkin_time);

COMMENT ON COLUMN crm_visit_record.customer_address_id IS '本次拜访校验的客户地址ID';
COMMENT ON COLUMN crm_visit_record.checkin_distance_meters IS '打卡坐标与客户地址坐标距离，单位米';
COMMENT ON COLUMN crm_visit_record.checkin_matched IS '打卡是否在允许半径内 0否 1是';
COMMENT ON COLUMN crm_visit_record.checkin_result IS 'MATCHED/MISMATCHED/NO_ADDRESS/UNCHECKED';
