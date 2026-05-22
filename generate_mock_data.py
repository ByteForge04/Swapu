import random
import time
from datetime import datetime, timedelta

# 配置
USER_COUNT = 30
ITEM_COUNT = 80
ORDER_COUNT = 40
WANT_COUNT = 30
COMMENT_COUNT = 25
NOTIFICATION_COUNT = 30
SEARCH_HISTORY_COUNT = 30

# 固定数据
PASSWORD_HASH = '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkMSghuEvPlpLn0YF/2' # 123456
CATEGORIES = [1, 2, 3, 4, 5, 6]
CAMPUS_AREAS = ['北校区', '南校区', '东校区', '西校区', '本部']
ITEM_TITLES = {
    1: ['考研数学复习全书', 'C++ Primer Plus', '高等数学同济版', '雅思真题4-14', '三体全集', '明朝那些事儿', 'Python编程从入门到实践', '经济学原理', '活着', '解忧杂货店'],
    2: ['iPhone 12', 'iPad Air 4', '罗技G304鼠标', '机械键盘', '小米手环', 'AirPods Pro', 'Switch游戏机', 'Kindle Paperwhite', '索尼耳机', '移动硬盘1T'],
    3: ['宿舍遮光帘', '小功率吹风机', '收纳箱', '台灯', '插排', '全身镜', '床上书桌', '暖壶', '晾衣架', '电风扇'],
    4: ['未开封口红', '防晒霜', '洗面奶', '面膜', '香水小样', '卷发棒', '化妆镜', '护手霜', '粉底液', '卸妆水'],
    5: ['尤尼克斯羽毛球拍', '篮球', '瑜伽垫', '哑铃', '滑板', '网球拍', '跳绳', '运动手环', '握力器', '轮滑鞋'],
    6: ['吉他', '尤克里里', '画板', '颜料套装', '演出门票', '超市购物卡', '校园网账号', '自行车', '电动车', '旧衣服']
}

sql_statements = []

def escape(s):
    return s.replace("'", "''")

def time_str(dt):
    return dt.strftime('%Y-%m-d %H:%M:%S')

start_time = datetime.now() - timedelta(days=90)

# 1. 生成用户
users = []
for i in range(1, USER_COUNT + 1):
    username = f'user{i}'
    nickname = f'同学{i}'
    phone = f'138{str(i).zfill(8)}'
    email = f'user{i}@example.com'
    created_at = start_time + timedelta(days=random.randint(0, 10))
    users.append({'id': i + 2, 'name': username}) # id从3开始，因为已有admin(1)和testuser(2)
    
    sql = f"INSERT INTO sys_user (username, password, email, phone, nickname, role, status, credit_score, created_at) VALUES ('{username}', '{PASSWORD_HASH}', '{email}', '{phone}', '{nickname}', 0, 1, {random.randint(80, 100)}, '{time_str(created_at)}');"
    sql_statements.append(sql)

# 2. 学生认证 (部分用户认证)
for user in users:
    if random.random() > 0.2:
        sql = f"INSERT INTO student_auth (user_id, real_name, student_no, college, major, entry_year, id_card_img, status, created_at) VALUES ({user['id']}, '张三{user['id']}', '2021{str(user['id']).zfill(4)}', '计算机学院', '软件工程', 2021, 'http://example.com/img.jpg', 1, '{time_str(start_time + timedelta(days=15))}');"
        sql_statements.append(sql)

# 3. 生成物品
items = []
for i in range(1, ITEM_COUNT + 1):
    user = random.choice(users)
    category_id = random.choice(CATEGORIES)
    title = random.choice(ITEM_TITLES[category_id]) + str(i)
    price = round(random.uniform(10, 500), 2)
    original_price = round(price * random.uniform(1.2, 2.0), 2)
    status = 1 # 默认在售
    created_at = start_time + timedelta(days=random.randint(20, 60))
    
    # 图片 JSON
    images = f"['https://via.placeholder.com/300?text=Item{i}']"
    
    sql = f"INSERT INTO item (user_id, category_id, title, description, price, original_price, images, condition_rate, transaction_method, status, view_count, want_count, campus_area, created_at) VALUES ({user['id']}, {category_id}, '{title}', '这是{title}的详细描述，九成新，需要的联系。', {price}, {original_price}, \"{images}\", {random.randint(5, 10)}, {random.randint(1, 3)}, {{status}}, {random.randint(0, 100)}, {random.randint(0, 20)}, '{random.choice(CAMPUS_AREAS)}', '{time_str(created_at)}');"
    
    # 暂存 SQL 模板，稍后根据订单更新状态
    items.append({'id': i, 'sql_template': sql, 'seller_id': user['id'], 'price': price, 'created_at': created_at, 'status': 1})

# 4. 生成订单 (部分物品被购买)
orders = []
used_item_ids = set()

for i in range(1, ORDER_COUNT + 1):
    # 随机选一个还未卖出的物品
    available_items = [item for item in items if item['id'] not in used_item_ids]
    if not available_items:
        break
        
    item = random.choice(available_items)
    used_item_ids.add(item['id'])
    
    # 买家不能是卖家
    possible_buyers = [u for u in users if u['id'] != item['seller_id']]
    if not possible_buyers:
        continue
    buyer = random.choice(possible_buyers)
    
    # 订单状态
    # 0-待确认, 1-进行中, 2-已完成, 3-已取消
    order_status = random.choices([0, 1, 2, 3], weights=[1, 2, 5, 1])[0]
    
    # 更新物品状态
    if order_status == 2:
        item['status'] = 3 # 已售出
    elif order_status == 1:
        item['status'] = 2 # 交易中
    elif order_status == 0:
        item['status'] = 2 # 交易中(锁定)
    else:
        item['status'] = 1 # 取消了还是在售
        used_item_ids.remove(item['id']) # 释放物品
    
    order_time = item['created_at'] + timedelta(days=random.randint(1, 10))
    pay_time = order_time + timedelta(minutes=random.randint(1, 30)) if order_status > 0 else 'NULL'
    completed_at = pay_time + timedelta(days=random.randint(1, 3)) if order_status == 2 else 'NULL'
    
    pay_time_val = f"'{time_str(pay_time)}'" if pay_time != 'NULL' else 'NULL'
    completed_at_val = f"'{time_str(completed_at)}'" if completed_at != 'NULL' else 'NULL'
    payment_status = 1 if order_status > 0 else 0
    
    order_no = f"ORD{time.strftime('%Y%m%d')}{str(i).zfill(6)}"
    
    sql = f"INSERT INTO trade_order (order_no, buyer_id, seller_id, item_id, amount, transaction_method, shipping_address, status, payment_status, trade_no, pay_time, created_at, completed_at) VALUES ('{order_no}', {buyer['id']}, {item['seller_id']}, {item['id']}, {item['price']}, 1, '学校南门', {order_status}, {payment_status}, 'ALIPAY_{order_no}', {pay_time_val}, '{time_str(order_time)}', {completed_at_val});"
    sql_statements.append(sql)
    
    if order_status == 2:
        orders.append({'id': i, 'buyer_id': buyer['id'], 'seller_id': item['seller_id'], 'item_id': item['id'], 'order_time': order_time})

# 补充物品 SQL
for item in items:
    sql_statements.append(item['sql_template'].format(status=item['status']))

# 5. 生成评价 (仅针对已完成订单)
for i in range(1, min(COMMENT_COUNT, len(orders)) + 1):
    order = orders[i-1]
    # 买家评价卖家
    sql = f"INSERT INTO comment (order_id, item_id, user_id, target_user_id, content, rating, created_at) VALUES ({order['id']}, {order['item_id']}, {order['buyer_id']}, {order['seller_id']}, '交易很愉快，东西很好！', 5, '{time_str(order['order_time'] + timedelta(days=3))}');"
    sql_statements.append(sql)

# 6. 生成收藏
for i in range(WANT_COUNT):
    user = random.choice(users)
    item = random.choice(items)
    # 避免重复
    sql = f"INSERT IGNORE INTO item_want (user_id, item_id, created_at) VALUES ({user['id']}, {item['id']}, '{time_str(start_time + timedelta(days=random.randint(10, 80)))}');"
    sql_statements.append(sql)

# 7. 生成通知
for i in range(NOTIFICATION_COUNT):
    user = random.choice(users)
    sql = f"INSERT INTO sys_notification (user_id, type, title, content, is_read, created_at) VALUES ({user['id']}, 1, '系统通知', '您的物品已被浏览超过100次', {random.randint(0, 1)}, '{time_str(start_time + timedelta(days=random.randint(50, 90)))}');"
    sql_statements.append(sql)

# 8. 搜索历史
keywords = ['自行车', '考研', '雅思', '手机', '显示器', '吉他', '滑板', '教材', '耳机', '键盘']
for kw in keywords:
    sql = f"INSERT INTO search_history (keyword, count) VALUES ('{kw}', {random.randint(10, 500)});"
    sql_statements.append(sql)

# 写入文件
with open('d:\\SwapU\\database\\mock_data.sql', 'w', encoding='utf-8') as f:
    f.write("USE swapu;\n")
    f.write("SET NAMES utf8mb4;\n")
    f.write("\n".join(sql_statements))

print(f"Generated {len(sql_statements)} SQL statements.")
