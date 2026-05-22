import json
import random
import time
from datetime import datetime, timedelta

# ==========================================
# SwapU 校园闲置平台 - 模拟商品数据生成脚本
# ==========================================
# 考虑到真正的爬虫（如闲鱼、转转）有极强的反爬机制，并且抓取图片等很容易失效，
# 这里采用【程序化生成高质量模拟数据】的方式。
# 生成的数据结构完全贴合当前 SwapU 项目的 MySQL item 表设计。
# ==========================================

# 预设的分类 ID 映射 (根据 init.sql)
# 1: 闲置书籍, 2: 数码产品, 3: 生活用品, 4: 美妆护肤, 5: 运动健身, 6: 其他闲置
CATEGORIES = {
    1: {
        "names": ["高等数学同济第七版上下册", "计算机网络自顶向下方法", "考研英语真题试卷", "全新未拆封四级词汇", "Python编程从入门到实践", "毛概思修教材打包", "深入理解计算机系统", "雅思真题4-14打包", "线性代数同济版", "数据结构与算法分析"],
        "desc_templates": ["学长毕业清仓，{name}，笔记记得很全，低价出。", "上课用过的{name}，保护得很好，无缺页漏页。", "买错版本的{name}，几乎全新，便宜转了！", "考研上岸，这本{name}带我拿了高分，传递好运！"],
        "images": ["https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1532012197267-da84d127e765?q=80&w=600&auto=format&fit=crop"]
    },
    2: {
        "names": ["AirPods Pro 2代无线耳机", "iPad Pro 2021 128G 银色", "罗技K840机械键盘", "索尼G304鼠标", "小米充电宝20000mAh", "二手iPhone 13 256G", "大疆降噪耳机", "二手微单相机索尼A6000", "显示器支架臂", "绿联无线鼠标"],
        "desc_templates": ["换了新设备，这套{name}用不上了，箱说全。", "自用{name}，功能一切正常，电池健康度很好。", "帮室友代出的{name}，平时很少用，外观无磕碰。", "{name}，刚买没多久，还在保修期内，低价慢出。"],
        "images": ["https://images.unsplash.com/photo-1546868871-7041f2a55e12?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1583394838336-acd977736f90?q=80&w=600&auto=format&fit=crop"]
    },
    3: {
        "names": ["宿舍床边桌/电脑桌", "落地全身镜", "多层鞋架收纳架", "寝室遮光床帘", "小熊迷你电饭煲", "桌面收纳盒抽屉", "折叠懒人椅", "静音小风扇", "衣服收纳箱大号", "宿舍用小台灯"],
        "desc_templates": ["毕业搬宿舍带不走，{name}便宜处理了。", "寝室神器{name}，非常实用，用了半个学期。", "全新没拆包的{name}，买多了用不上。", "{name}，洗干净了，直接拿走就能用。"],
        "images": ["https://images.unsplash.com/photo-1505693314120-0d443867891c?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1513694203232-719a280e022f?q=80&w=600&auto=format&fit=crop"]
    },
    4: {
        "names": ["全新未拆封MAC口红", "雅诗兰黛小棕瓶眼霜中小样", "SK2神仙水余量一半", "海蓝之谜洗面奶", "完美日记粉底液", "无印良品化妆棉", "欧莱雅隔离霜", "理肤泉男士洗面奶", "科颜氏B5乳液", "兰蔻小黑瓶精华"],
        "desc_templates": ["囤货太多用不完，{name}绝对正品，支持验货。", "不适合我的肤质，{name}仅试色一次。", "{name}，专柜赠品小样，打包低价出。", "{name}，有效期还很长，看图确认余量。"],
        "images": ["https://images.unsplash.com/photo-1596462502278-27bf85033e5a?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1522337360788-8b13fee7a3af?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1599305090598-fe179d501227?q=80&w=600&auto=format&fit=crop"]
    },
    5: {
        "names": ["李宁7号篮球", "尤尼克斯羽毛球拍", "家用瑜伽垫加厚", "哑铃一对5kg", "跳绳", "健身握力器", "斯伯丁排球", "运动护膝护腕套", "俯卧撑支架", "拉力器"],
        "desc_templates": ["没时间运动了，{name}出给有缘人。", "买来就在宿舍吃灰的{name}，基本上是全新的。", "加入了院队换了新装备，这套{name}低价出。", "{name}，质量很好，可以先看货再决定。"],
        "images": ["https://images.unsplash.com/photo-1515523110800-9415d13b84a8?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=600&auto=format&fit=crop"]
    },
    6: {
        "names": ["二手吉他带琴包", "动漫周边盲盒手办", "闲置卡牌卡带", "手工毛线编织物", "演唱会荧光棒", "二手滑板", "拼图画", "桌游卡牌UNO", "大富翁纸牌", "定制钥匙扣"],
        "desc_templates": ["退坑回血，{name}全部打包出。", "收拾柜子翻出来的{name}，寻找同好。", "闲置的{name}，可以用来当装饰品。", "{name}，价格好商量，只求带走。"],
        "images": ["https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=600&auto=format&fit=crop", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop"]
    }
}

CAMPUS_AREAS = ["北校区", "南校区", "东校区", "西校区", "大学城校区", "图书馆一楼", "第一食堂门口", "三号宿舍楼下"]

def random_date(start_days_ago=30):
    """生成过去30天内的随机时间"""
    now = datetime.now()
    random_days = random.randint(0, start_days_ago)
    random_seconds = random.randint(0, 86400)
    past_date = now - timedelta(days=random_days, seconds=random_seconds)
    return past_date.strftime('%Y-%m-%d %H:%M:%S')

def generate_mock_data(num_records=50):
    sql_statements = []
    sql_statements.append("-- SwapU 商品模拟数据集")
    sql_statements.append("SET NAMES utf8mb4;")
    sql_statements.append("USE `swapu`;")
    sql_statements.append("")
    
    # 假设你的数据库里已经有 user_id 为 1, 2, 3 的测试用户
    user_ids = [1, 2, 3]

    for i in range(num_records):
        # 随机选择一个分类
        category_id = random.choice(list(CATEGORIES.keys()))
        cat_data = CATEGORIES[category_id]
        
        # 随机生成标题和描述
        item_name = random.choice(cat_data["names"])
        title = item_name
        description = random.choice(cat_data["desc_templates"]).format(name=item_name)
        
        # 随机生成价格
        original_price = round(random.uniform(20.0, 1000.0), 2)
        price = round(original_price * random.uniform(0.1, 0.8), 2) # 折扣在1-8折之间
        
        # 随机图片 (1-3张)
        num_images = random.randint(1, 3)
        images_list = random.sample(cat_data["images"], min(num_images, len(cat_data["images"])))
        images_json = json.dumps(images_list, ensure_ascii=False)
        
        # 其他随机属性
        user_id = random.choice(user_ids)
        condition_rate = random.randint(6, 10) # 6-10成新
        transaction_method = random.randint(1, 3)
        status = 1 # 1: 在售
        view_count = random.randint(0, 500)
        want_count = random.randint(0, 50)
        campus_area = random.choice(CAMPUS_AREAS)
        created_at = random_date()
        
        # 构造 SQL 语句
        sql = f"""INSERT INTO `item` (`user_id`, `category_id`, `title`, `description`, `price`, `original_price`, `images`, `condition_rate`, `transaction_method`, `status`, `view_count`, `want_count`, `campus_area`, `created_at`, `updated_at`) VALUES ({user_id}, {category_id}, '{title}', '{description}', {price}, {original_price}, '{images_json}', {condition_rate}, {transaction_method}, {status}, {view_count}, {want_count}, '{campus_area}', '{created_at}', '{created_at}');"""
        
        sql_statements.append(sql)

    return "\n".join(sql_statements)

if __name__ == "__main__":
    # 生成 60 条模拟数据
    sql_script = generate_mock_data(60)
    
    # 写入文件
    with open('d:\\SwapU\\database\\mock_items.sql', 'w', encoding='utf-8') as f:
        f.write(sql_script)
    
    print("生成成功！SQL 脚本已保存至 d:\\SwapU\\database\\mock_items.sql")
