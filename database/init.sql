-- 数据库初始化
DROP DATABASE IF EXISTS `swapu`;
CREATE DATABASE IF NOT EXISTS `swapu` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `swapu`;

-- 1. 用户表 (sys_user)
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/账号 (唯一)',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码 (BCrypt)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '绑定邮箱',
  `phone` VARCHAR(255) DEFAULT NULL COMMENT '绑定手机号',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通学生, 1-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-待审核',
  `credit_score` INT DEFAULT 100 COMMENT '信用分 (初始100, 基于交易评价增减)',
  `wechat_id` VARCHAR(50) DEFAULT NULL COMMENT '微信号 (用于交易联系)',
  `qq_id` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号 (用于交易联系)',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 学生认证表 (student_auth)
CREATE TABLE IF NOT EXISTS `student_auth` (
  `auth_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '认证记录ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '所属学院',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `entry_year` INT DEFAULT NULL COMMENT '入学年份 (如2023)',
  `id_card_img` VARCHAR(255) NOT NULL COMMENT '学生证/校园卡照片URL',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-审核通过, 2-审核驳回',
  `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注/驳回原因',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`auth_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生认证表';

-- 3. 物品分类表 (item_category)
CREATE TABLE IF NOT EXISTS `item_category` (
  `category_id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类表';

-- 初始化分类数据
INSERT INTO `item_category` (`category_name`, `icon`, `sort_order`) VALUES 
('闲置书籍', 'book', 1),
('数码产品', 'iphone', 2),
('生活用品', 'tableware', 3),
('美妆护肤', 'lipstick', 4),
('运动健身', 'basketball', 5),
('其他闲置', 'more', 6);

-- 4. 物品表 (item)
CREATE TABLE IF NOT EXISTS `item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '物品标题',
  `description` TEXT NOT NULL COMMENT '物品详细描述',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '出售价格 (0表示免费/赠送)',
  `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价 (作为参考)',
  `images` TEXT NOT NULL COMMENT '物品图片URL列表 (JSON数组格式)',
  `condition_rate` TINYINT DEFAULT 9 COMMENT '新旧程度: 1-10成新',
  `transaction_method` TINYINT DEFAULT 1 COMMENT '交易方式: 1-自提, 2-送货, 3-快递',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待审核, 1-在售, 2-交易中, 3-已售出, 4-已下架',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `want_count` INT DEFAULT 0 COMMENT '想要/收藏数',
  `campus_area` VARCHAR(50) DEFAULT NULL COMMENT '所属校区',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闲置物品表';

-- 5. 物品收藏表 (item_want)
CREATE TABLE IF NOT EXISTS `item_want` (
  `want_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`want_id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品收藏/想要表';

-- 6. 订单表 (trade_order)
CREATE TABLE IF NOT EXISTS `trade_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号 (唯一标识)',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '交易金额',
  `transaction_method` TINYINT DEFAULT 1 COMMENT '交易方式: 1-自提, 2-送货上门, 3-快递/邮寄',
  `shipping_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址/交易地点快照',
  `status` TINYINT DEFAULT 0 COMMENT '订单状态: 0-待卖家确认, 1-进行中/待交付, 2-已完成, 3-已取消',
  `payment_status` INT DEFAULT 0 COMMENT '0-未支付, 1-已支付',
  `payment_method` INT DEFAULT 1 COMMENT '1-线上支付, 2-线下自行交易',
  `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `buyer_note` VARCHAR(255) DEFAULT NULL COMMENT '买家备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 (下单时间)',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_item_id` (`item_id`)   
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 7. 评价表 (comment)
CREATE TABLE IF NOT EXISTS `comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `item_id` bigint NOT NULL COMMENT '关联物品ID',
  `user_id` bigint NOT NULL COMMENT '评价人ID',
  `target_user_id` bigint NOT NULL COMMENT '被评价人ID',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `rating` int NOT NULL DEFAULT '5' COMMENT '评分 1-5',
  `images` text COMMENT '评价图片 JSON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`)    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 8. 举报投诉表 (report)
CREATE TABLE IF NOT EXISTS `report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_id` bigint NOT NULL COMMENT '被举报对象ID(物品ID)',
  `type` int NOT NULL DEFAULT '1' COMMENT '类型 1-违规物品 2-交易纠纷',
  `reason` varchar(500) NOT NULL COMMENT '举报原因',
  `images` text COMMENT '凭证图片 JSON',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态 0-待处理 1-已处理 2-已驳回',
  `result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报投诉表';

-- 9. 系统通知表 (sys_notification)
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `notification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Notification ID',
  `user_id` BIGINT NOT NULL COMMENT 'Receiver User ID',
  `type` TINYINT NOT NULL COMMENT 'Type: 1-System, 2-Trade, 3-Interaction',
  `title` VARCHAR(100) NOT NULL COMMENT 'Title',
  `content` TEXT NOT NULL COMMENT 'Content',
  `related_id` BIGINT DEFAULT NULL COMMENT 'Related Object ID',
  `is_read` TINYINT DEFAULT 0 COMMENT 'Is Read: 0-No, 1-Yes',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  PRIMARY KEY (`notification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Notification Table';

-- 10. 系统公告表 (sys_announcement)
CREATE TABLE IF NOT EXISTS `sys_announcement` (
  `announcement_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-发布',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 插入一条示例公告
INSERT INTO `sys_announcement` (`title`, `content`, `status`) VALUES ('欢迎使用 SwapU', 'SwapU 校园二手交易平台正式上线啦！欢迎大家踊跃发布闲置物品。', 1);

-- 11. 搜索历史表 (search_history)
CREATE TABLE IF NOT EXISTS `search_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(255) NOT NULL,
  `count` INT DEFAULT 1,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- 12. 私信聊天记录表 (chat_message)
CREATE TABLE IF NOT EXISTS `chat_message` (
  `msg_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息主键ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `msg_type` TINYINT DEFAULT 1 COMMENT '消息类型: 1-文本, 2-图片, 3-商品卡片',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID(如商品ID)',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`msg_id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信聊天记录表';

-- 13. 物品留言板表 (item_message)
CREATE TABLE IF NOT EXISTS `item_message` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `item_id` BIGINT NOT NULL COMMENT '关联物品ID',
  `user_id` BIGINT NOT NULL COMMENT '留言用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '留言内容',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父留言ID(如果是回复)',
  `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复目标用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_item_id` (`item_id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品留言板表';

-- 14. 管理员账号初始化
-- 密码默认为 123456 (BCrypt 加密)
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `role`, `status`) 
VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPlpLn0YF/2', '超级管理员', 1, 1),
(2, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPlpLn0YF/2', '测试同学', 0, 1),
(3, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPlpLn0YF/2', '测试同学2', 0, 1);
