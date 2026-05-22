USE `swapu`;

-- 1. Reset/Clean existing mock data (optional, be careful in production)
-- TRUNCATE TABLE `sys_notification`;
-- TRUNCATE TABLE `trade_order`;
-- TRUNCATE TABLE `item_want`;
-- TRUNCATE TABLE `item_comment`;
-- TRUNCATE TABLE `item`;
-- DELETE FROM `sys_user` WHERE username LIKE 'student%';

-- 2. Insert Users (20 students)
-- Note: 'campus' field is not in sys_user table, so we don't insert it.
INSERT IGNORE INTO `sys_user` (username, password, email, phone, nickname, avatar, role, status) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student1@test.com', '13800000001', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', 0, 1),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student2@test.com', '13800000002', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', 0, 1),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student3@test.com', '13800000003', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', 0, 1),
('student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student4@test.com', '13800000004', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', 0, 1),
('student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student5@test.com', '13800000005', '陈七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=5', 0, 1),
('student6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student6@test.com', '13800000006', '刘八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=6', 0, 1),
('student7', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student7@test.com', '13800000007', '孙九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=7', 0, 1),
('student8', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student8@test.com', '13800000008', '周十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=8', 0, 1),
('student9', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student9@test.com', '13800000009', '吴十一', 'https://api.dicebear.com/7.x/avataaars/svg?seed=9', 0, 1),
('student10', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student10@test.com', '13800000010', '郑十二', 'https://api.dicebear.com/7.x/avataaars/svg?seed=10', 0, 1),
('student11', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student11@test.com', '13800000011', '钱十三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=11', 0, 1),
('student12', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student12@test.com', '13800000012', '冯十四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=12', 0, 1),
('student13', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student13@test.com', '13800000013', '陈十五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=13', 0, 1),
('student14', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student14@test.com', '13800000014', '褚十六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=14', 0, 1),
('student15', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student15@test.com', '13800000015', '卫十七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=15', 0, 1),
('student16', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student16@test.com', '13800000016', '蒋十八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=16', 0, 1),
('student17', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student17@test.com', '13800000017', '沈十九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=17', 0, 1),
('student18', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student18@test.com', '13800000018', '韩二十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=18', 0, 1),
('student19', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student19@test.com', '13800000019', '杨二十一', 'https://api.dicebear.com/7.x/avataaars/svg?seed=19', 0, 1),
('student20', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd.g.w1.E.a', 'student20@test.com', '13800000020', '朱二十二', 'https://api.dicebear.com/7.x/avataaars/svg?seed=20', 0, 1);

-- 3. Procedure to generate 200 items
DELIMITER $$
DROP PROCEDURE IF EXISTS generate_mock_items$$
CREATE PROCEDURE generate_mock_items()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE u_id BIGINT;
    DECLARE c_id INT;
    DECLARE price DECIMAL(10,2);
    DECLARE cond TINYINT;
    DECLARE stat TINYINT;
    DECLARE title_prefix VARCHAR(50);
    DECLARE desc_text TEXT;
    
    -- Ensure we have items starting from ID 1 or continue
    -- But we will just loop 200 times
    
    WHILE i <= 200 DO
        -- Random user from existing users
        SELECT user_id INTO u_id FROM sys_user ORDER BY RAND() LIMIT 1;
        
        -- Random category 1-6
        SET c_id = FLOOR(1 + RAND() * 6);
        
        -- Random price 10-500
        SET price = FLOOR(10 + RAND() * 490);
        
        -- Random condition 5-10
        SET cond = FLOOR(5 + RAND() * 6);
        
        -- Status: 80% On Sale (1), 10% Sold (3), 10% In Transaction (2)
        IF RAND() < 0.8 THEN
            SET stat = 1;
        ELSEIF RAND() < 0.9 THEN
            SET stat = 2;
        ELSE
            SET stat = 3;
        END IF;
        
        -- Title and Description based on category
        IF c_id = 1 THEN 
            SET title_prefix = '二手教材/书籍';
            SET desc_text = '考研/考公/期末复习必备资料，笔记全，保存良好。';
        ELSEIF c_id = 2 THEN 
            SET title_prefix = '数码/电子产品';
            SET desc_text = '闲置电子产品，功能正常，配件齐全，可小刀。';
        ELSEIF c_id = 3 THEN 
            SET title_prefix = '生活用品';
            SET desc_text = '宿舍神器，毕业带不走，低价转让。';
        ELSEIF c_id = 4 THEN 
            SET title_prefix = '美妆护肤';
            SET desc_text = '专柜正品，用量很少，日期新鲜。';
        ELSEIF c_id = 5 THEN 
            SET title_prefix = '运动器材';
            SET desc_text = '强身健体，买来没怎么用过，99新。';
        ELSE 
            SET title_prefix = '其他闲置';
            SET desc_text = '杂七杂八的小东西，看着给价吧。';
        END IF;
        
        INSERT INTO `item` (user_id, category_id, title, description, price, original_price, images, condition_rate, transaction_method, status, view_count, want_count, campus_area)
        VALUES (
            u_id, 
            c_id, 
            CONCAT(title_prefix, ' ', i), 
            CONCAT(desc_text, ' (编号:', i, ')'), 
            price, 
            price * 1.5, 
            '["https://via.placeholder.com/300x300.png?text=Item+Image"]', 
            cond, 
            FLOOR(1 + RAND() * 3), 
            stat,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 50),
            IF(RAND() > 0.5, '南校区', '北校区')
        );
        
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL generate_mock_items();
DROP PROCEDURE generate_mock_items;

-- 4. Generate some "Want" (Favorites)
INSERT INTO `item_want` (user_id, item_id)
SELECT 
    u.user_id, 
    i.item_id 
FROM `item` i
JOIN `sys_user` u ON u.user_id != i.user_id
WHERE RAND() < 0.05 
LIMIT 50;

-- 5. Generate some Comments
INSERT INTO `item_comment` (item_id, user_id, content)
SELECT 
    i.item_id,
    u.user_id,
    '请问还在吗？可以便宜点吗？'
FROM `item` i
JOIN `sys_user` u ON u.user_id != i.user_id
WHERE RAND() < 0.03
LIMIT 30;

-- 6. Generate Orders for Sold Items (Status = 3)
-- Only create orders for items that are actually marked as Sold (3)
INSERT INTO `trade_order` (order_no, buyer_id, seller_id, item_id, amount, status, shipping_address)
SELECT 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(i.item_id, 6, '0')),
    (SELECT user_id FROM sys_user WHERE user_id != i.user_id ORDER BY RAND() LIMIT 1),
    i.user_id,
    i.item_id,
    i.price,
    2, -- Completed
    '南校区1号楼'
FROM `item` i
WHERE i.status = 3;

-- 7. Generate System Notifications
INSERT INTO `sys_notification` (user_id, type, title, content)
SELECT 
    user_id, 
    1, 
    '欢迎加入SwapU', 
    '祝您交易愉快！' 
FROM `sys_user` 
LIMIT 20;

-- 8. Add some Announcements
INSERT INTO `sys_announcement` (title, content, status) VALUES 
('系统维护通知', '本周日凌晨进行系统维护，预计1小时。', 1),
('防诈骗提醒', '交易请尽量选择线下自提或平台担保，谨防诈骗。', 1),
('新功能上线', '搜索功能已升级，支持自动补全和模糊搜索！', 1);
