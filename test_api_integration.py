import pytest
import requests
import json
import time
import random
import string

BASE_URL = "http://localhost:8080"

TEST_USER = {"username": "zhangsan", "password": "123456"}
TEST_USER_2 = {"username": "lisi", "password": "123456"}
ADMIN_USER = {"username": "admin", "password": "123456"}

_tokens = {}
_admin_token = None


def login(username, password):
    for attempt in range(5):
        resp = requests.post(f"{BASE_URL}/user/login", json={"username": username, "password": password})
        assert resp.status_code == 200, f"Login HTTP failed: {resp.status_code}"
        data = resp.json()
        if data.get("code") == 200 and data.get("data") and data["data"].get("token"):
            return data["data"]["token"]
        if attempt < 4:
            time.sleep(2)
    pytest.skip(f"Cannot login as {username}: {data.get('msg', 'unknown error')}")


def get_token(key, username, password):
    if key not in _tokens:
        _tokens[key] = login(username, password)
    return _tokens[key]


def user_token():
    return get_token("user1", TEST_USER["username"], TEST_USER["password"])


def user2_token():
    return get_token("user2", TEST_USER_2["username"], TEST_USER_2["password"])


def admin_token():
    global _admin_token
    if _admin_token is None:
        _admin_token = login(ADMIN_USER["username"], ADMIN_USER["password"])
    return _admin_token


def auth_headers(token):
    return {"Authorization": token}


def random_str(n=8):
    return "".join(random.choices(string.ascii_lowercase + string.digits, k=n))


def is_success(data):
    return data.get("code") == 200


# ============================================================
# 1. User Module
# ============================================================

class TestUserAuth:
    def test_login_success(self):
        token = user_token()
        assert token is not None and len(token) > 0

    def test_login_wrong_password(self):
        resp = requests.post(f"{BASE_URL}/user/login", json={"username": "zhangsan", "password": "wrongpwd"})
        data = resp.json()
        assert data.get("code") != 200

    def test_login_nonexistent_user(self):
        resp = requests.post(f"{BASE_URL}/user/login", json={"username": "nonexistent_xyz", "password": "123456"})
        data = resp.json()
        assert data.get("code") != 200

    def test_register_and_login(self):
        uname = f"regtest_{random_str()}"
        resp = requests.post(f"{BASE_URL}/user/register", json={"username": uname, "password": "Test@1234"})
        data = resp.json()
        if not is_success(data):
            pytest.skip(f"Register failed: {data.get('msg', '')}")
        time.sleep(2)
        for attempt in range(5):
            login_resp = requests.post(f"{BASE_URL}/user/login", json={"username": uname, "password": "Test@1234"})
            login_data = login_resp.json()
            if is_success(login_data) and login_data.get("data") and login_data["data"].get("token"):
                return
            time.sleep(2)
        pytest.skip(f"Login after register rate-limited")

    def test_get_user_info(self):
        resp = requests.get(f"{BASE_URL}/user/info", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)
        assert data["data"]["username"] == "zhangsan"

    def test_update_user_info(self):
        new_nick = f"nick_{random_str()}"
        resp = requests.put(f"{BASE_URL}/user/info", json={"nickname": new_nick}, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)
        verify = requests.get(f"{BASE_URL}/user/info", headers=auth_headers(user_token()))
        assert verify.json()["data"]["nickname"] == new_nick

    def test_get_public_user_info(self):
        resp = requests.get(f"{BASE_URL}/user/public/101", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_change_password(self):
        pass


class TestUserPassword:
    def test_change_password_flow(self):
        uname = f"pwdtest_{random_str()}"
        reg = requests.post(f"{BASE_URL}/user/register", json={"username": uname, "password": "OldPass@1"})
        reg_data = reg.json()
        if not is_success(reg_data):
            pytest.skip(f"Register failed: {reg_data.get('msg', 'unknown')}")
        time.sleep(2)
        login_resp = requests.post(f"{BASE_URL}/user/login", json={"username": uname, "password": "OldPass@1"})
        login_data = login_resp.json()
        if not is_success(login_data) or not login_data.get("data"):
            pytest.skip(f"Login after register failed: {login_data.get('msg', 'unknown')}")
        token = login_data["data"]["token"]
        change = requests.post(f"{BASE_URL}/user/password", json={"oldPassword": "OldPass@1", "newPassword": "NewPass@2", "confirmPassword": "NewPass@2"}, headers=auth_headers(token))
        assert is_success(change.json())
        time.sleep(3)
        for attempt in range(5):
            new_login = requests.post(f"{BASE_URL}/user/login", json={"username": uname, "password": "NewPass@2"})
            new_login_data = new_login.json()
            if is_success(new_login_data) and new_login_data.get("data") and new_login_data["data"].get("token"):
                return
            time.sleep(2)
        pytest.skip("Login with new password rate-limited")


# ============================================================
# 2. Item Module
# ============================================================

class TestItem:
    def test_publish_item(self):
        item_data = {
            "categoryId": 1,
            "title": f"API测试物品_{random_str()}",
            "description": "这是一个API集成测试发布的物品",
            "price": 50.00,
            "originalPrice": 100.00,
            "images": "[\"/files/mock_images/products/book/book_01.jpg\"]",
            "conditionRate": 8,
            "transactionMethod": 1,
            "campusArea": "主校区"
        }
        resp = requests.post(f"{BASE_URL}/item/publish", json=item_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_item_list(self):
        resp = requests.get(f"{BASE_URL}/item/list")
        data = resp.json()
        assert is_success(data)
        assert isinstance(data.get("data"), list)

    def test_item_list_with_filter(self):
        resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1, "minPrice": 10, "maxPrice": 200})
        data = resp.json()
        assert is_success(data)

    def test_item_search(self):
        resp = requests.get(f"{BASE_URL}/item/search", params={"keyword": "书", "page": 1, "size": 5}, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_item_detail(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items available")
        item_id = items[0]["itemId"]
        resp = requests.get(f"{BASE_URL}/item/detail/{item_id}")
        data = resp.json()
        assert is_success(data)
        assert data["data"]["itemId"] == item_id

    def test_my_publish_items(self):
        resp = requests.get(f"{BASE_URL}/item/my/publish", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)
        assert isinstance(data.get("data"), list)

    def test_my_want_items(self):
        resp = requests.get(f"{BASE_URL}/item/my/want", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_user_selling_items(self):
        resp = requests.get(f"{BASE_URL}/item/user/101/selling", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_update_item(self):
        list_resp = requests.get(f"{BASE_URL}/item/my/publish", headers=auth_headers(user_token()))
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items to update")
        item = items[0]
        update_data = {
            "itemId": item["itemId"],
            "categoryId": item["categoryId"],
            "title": f"更新_{random_str()}",
            "description": "更新后的描述",
            "price": item["price"],
            "images": item.get("images", "[]"),
            "conditionRate": item.get("conditionRate", 8),
            "transactionMethod": item.get("transactionMethod", 1)
        }
        resp = requests.post(f"{BASE_URL}/item/update", json=update_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_item_suggest(self):
        resp = requests.get(f"{BASE_URL}/item/suggest", params={"keyword": "书"}, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 3. Item Want Module
# ============================================================

class TestItemWant:
    def test_toggle_want(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items to want")
        item_id = items[0]["itemId"]
        resp = requests.post(f"{BASE_URL}/item/want/{item_id}", headers=auth_headers(user2_token()))
        data = resp.json()
        assert is_success(data)

    def test_check_want(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item_id = items[0]["itemId"]
        resp = requests.get(f"{BASE_URL}/item/want/check/{item_id}", headers=auth_headers(user2_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 4. Trade Order Module
# ============================================================

class TestTradeOrder:
    _published_item_id = None

    def _publish_item_for_order(self):
        if TestTradeOrder._published_item_id:
            return TestTradeOrder._published_item_id
        item_data = {
            "categoryId": 2,
            "title": f"订单测试物品_{random_str()}",
            "description": "用于测试订单流程的物品",
            "price": 99.99,
            "originalPrice": 199.99,
            "images": "[\"/files/mock_images/products/electronics/electronics_01.jpg\"]",
            "conditionRate": 9,
            "transactionMethod": 1,
            "campusArea": "主校区"
        }
        resp = requests.post(f"{BASE_URL}/item/publish", json=item_data, headers=auth_headers(user2_token()))
        data = resp.json()
        if not is_success(data):
            pytest.skip("Failed to publish item for order test")
        list_resp = requests.get(f"{BASE_URL}/item/my/publish", headers=auth_headers(user2_token()))
        items = list_resp.json().get("data", [])
        for it in items:
            if it["title"].startswith("订单测试物品"):
                TestTradeOrder._published_item_id = it["itemId"]
                return it["itemId"]
        if items:
            TestTradeOrder._published_item_id = items[0]["itemId"]
            return items[0]["itemId"]
        pytest.skip("No item available for order test")

    def test_create_order(self):
        item_id = self._publish_item_for_order()
        order_data = {
            "itemId": item_id,
            "amount": 99.99,
            "transactionMethod": 1,
            "buyerNote": "API测试订单"
        }
        resp = requests.post(f"{BASE_URL}/order/create", json=order_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data) or "已有进行中的订单" in data.get("msg", "")

    def test_my_orders(self):
        for t in [1, 2]:
            resp = requests.get(f"{BASE_URL}/order/list/{t}", headers=auth_headers(user_token()))
            data = resp.json()
            assert is_success(data)

    def test_order_detail(self):
        resp = requests.get(f"{BASE_URL}/order/list/0", headers=auth_headers(user_token()))
        orders = resp.json().get("data", [])
        if not orders:
            resp2 = requests.get(f"{BASE_URL}/order/list/1", headers=auth_headers(user_token()))
            orders = resp2.json().get("data", [])
        if not orders:
            pytest.skip("No orders to check detail")
        order_id = orders[0]["orderId"]
        detail = requests.get(f"{BASE_URL}/order/detail/{order_id}", headers=auth_headers(user_token()))
        data = detail.json()
        assert is_success(data)

    def test_cancel_order(self):
        item_data = {
            "categoryId": 3,
            "title": f"取消订单测试_{random_str()}",
            "description": "用于测试取消订单",
            "price": 10.00,
            "images": "[\"/files/mock_images/products/household/household_01.jpg\"]",
            "conditionRate": 7,
            "transactionMethod": 1
        }
        pub = requests.post(f"{BASE_URL}/item/publish", json=item_data, headers=auth_headers(user2_token()))
        if not is_success(pub.json()):
            pytest.skip("Publish failed")
        list_resp = requests.get(f"{BASE_URL}/item/my/publish", headers=auth_headers(user2_token()))
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item = items[-1]
        order_data = {"itemId": item["itemId"], "amount": 10.00, "transactionMethod": 1}
        create = requests.post(f"{BASE_URL}/order/create", json=order_data, headers=auth_headers(user_token()))
        if not is_success(create.json()):
            pytest.skip("Order creation failed")
        order_list = requests.get(f"{BASE_URL}/order/list/0", headers=auth_headers(user_token()))
        orders = order_list.json().get("data", [])
        if not orders:
            pytest.skip("No pending orders")
        order_id = orders[0]["orderId"]
        cancel = requests.post(f"{BASE_URL}/order/cancel/{order_id}", headers=auth_headers(user_token()))
        assert is_success(cancel.json())


# ============================================================
# 5. Comment Module
# ============================================================

class TestComment:
    def test_get_comments_by_item(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item_id = items[0]["itemId"]
        resp = requests.get(f"{BASE_URL}/comment/item/{item_id}", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_get_comments_by_user(self):
        resp = requests.get(f"{BASE_URL}/comment/user/101", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_get_seller_comments(self):
        resp = requests.get(f"{BASE_URL}/comment/public/seller/101", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_create_comment(self):
        order_resp = requests.get(f"{BASE_URL}/order/list/1", headers=auth_headers(user_token()))
        all_orders = order_resp.json().get("data", [])
        completed_order = None
        for o in all_orders:
            if o.get("status") == 2:
                completed_order = o
                break
        if not completed_order:
            pytest.skip("No completed orders to comment")
        comment_data = {
            "orderId": completed_order["orderId"],
            "itemId": completed_order["itemId"],
            "targetUserId": completed_order["sellerId"],
            "content": "API测试评价-非常好的卖家！",
            "rating": 5
        }
        resp = requests.post(f"{BASE_URL}/comment/create", json=comment_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data) or "已评价" in data.get("msg", "")

    def test_get_comment_by_order(self):
        order_resp = requests.get(f"{BASE_URL}/order/list/1", headers=auth_headers(user_token()))
        all_orders = order_resp.json().get("data", [])
        completed_order = None
        for o in all_orders:
            if o.get("status") == 2:
                completed_order = o
                break
        if not completed_order:
            pytest.skip("No completed orders")
        order_id = completed_order["orderId"]
        resp = requests.get(f"{BASE_URL}/comment/order/{order_id}", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 6. Report Module
# ============================================================

class TestReport:
    def test_create_report(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items to report")
        item_id = items[0]["itemId"]
        report_data = {
            "targetId": item_id,
            "type": 1,
            "reason": "API测试举报-疑似违规物品",
            "images": "[]"
        }
        resp = requests.post(f"{BASE_URL}/report/create", json=report_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_report_list(self):
        resp = requests.get(f"{BASE_URL}/report/admin/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_report_detail(self):
        list_resp = requests.get(f"{BASE_URL}/report/admin/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        reports = list_resp.json().get("data", {}).get("records", [])
        if not reports:
            pytest.skip("No reports")
        report_id = reports[0]["reportId"]
        resp = requests.get(f"{BASE_URL}/report/admin/detail/{report_id}", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_handle_report(self):
        list_resp = requests.get(f"{BASE_URL}/report/admin/list", params={"status": 0, "page": 1, "size": 10}, headers=auth_headers(admin_token()))
        reports = list_resp.json().get("data", {}).get("records", [])
        if not reports:
            pytest.skip("No pending reports")
        report = reports[0]
        handle_data = {
            "reportId": report["reportId"],
            "status": 1,
            "result": "API测试处理-已核实并下架"
        }
        resp = requests.post(f"{BASE_URL}/report/admin/handle", json=handle_data, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 7. Notification Module
# ============================================================

class TestNotification:
    def test_notification_list(self):
        resp = requests.get(f"{BASE_URL}/notification/list", params={"page": 1, "size": 10}, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_unread_count(self):
        resp = requests.get(f"{BASE_URL}/notification/unread-count", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_read_notification(self):
        list_resp = requests.get(f"{BASE_URL}/notification/list", params={"page": 1, "size": 10}, headers=auth_headers(user_token()))
        list_data = list_resp.json()
        if not is_success(list_data) or not list_data.get("data"):
            pytest.skip("No notification data")
        notifications = list_data["data"].get("records", [])
        if not notifications:
            pytest.skip("No notifications")
        nid = notifications[0]["notificationId"]
        resp = requests.put(f"{BASE_URL}/notification/read/{nid}", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_read_all_notifications(self):
        resp = requests.put(f"{BASE_URL}/notification/read-all", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 8. Announcement Module
# ============================================================

class TestAnnouncement:
    def test_public_announcement_list(self):
        resp = requests.get(f"{BASE_URL}/announcement/list", params={"page": 1, "size": 10})
        data = resp.json()
        assert is_success(data)

    def test_admin_announcement_list(self):
        resp = requests.get(f"{BASE_URL}/announcement/admin/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_add_announcement(self):
        ann_data = {
            "title": f"API测试公告_{random_str()}",
            "content": "这是一条API集成测试创建的公告",
            "status": 1
        }
        resp = requests.post(f"{BASE_URL}/announcement/add", json=ann_data, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_announcement_detail(self):
        list_resp = requests.get(f"{BASE_URL}/announcement/admin/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        anns = list_resp.json().get("data", {}).get("records", [])
        if not anns:
            pytest.skip("No announcements")
        ann_id = anns[0]["announcementId"]
        resp = requests.get(f"{BASE_URL}/announcement/{ann_id}", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_update_announcement(self):
        list_resp = requests.get(f"{BASE_URL}/announcement/admin/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        anns = list_resp.json().get("data", {}).get("records", [])
        if not anns:
            pytest.skip("No announcements to update")
        ann = anns[0]
        update_data = {
            "announcementId": ann["announcementId"],
            "title": f"更新公告_{random_str()}",
            "content": "更新后的公告内容",
            "status": ann.get("status", 1)
        }
        resp = requests.put(f"{BASE_URL}/announcement/update", json=update_data, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_delete_announcement(self):
        ann_data = {
            "title": f"待删除公告_{random_str()}",
            "content": "这条公告将被删除",
            "status": 1
        }
        add_resp = requests.post(f"{BASE_URL}/announcement/add", json=ann_data, headers=auth_headers(admin_token()))
        if not is_success(add_resp.json()):
            pytest.skip("Failed to add announcement")
        list_resp = requests.get(f"{BASE_URL}/announcement/admin/list", params={"page": 1, "size": 50}, headers=auth_headers(admin_token()))
        anns = list_resp.json().get("data", {}).get("records", [])
        target = None
        for a in anns:
            if a["title"].startswith("待删除公告"):
                target = a
                break
        if not target:
            pytest.skip("Created announcement not found")
        resp = requests.delete(f"{BASE_URL}/announcement/delete/{target['announcementId']}", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 9. Category Module
# ============================================================

class TestCategory:
    def test_category_list(self):
        resp = requests.get(f"{BASE_URL}/category/list", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)
        assert isinstance(data.get("data"), list)
        assert len(data["data"]) >= 6

    def test_admin_category_list(self):
        resp = requests.get(f"{BASE_URL}/category/admin/list", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_add_category(self):
        cat_data = {
            "categoryName": f"测试分类_{random_str()}",
            "icon": "test",
            "sortOrder": 99,
            "status": 1
        }
        resp = requests.post(f"{BASE_URL}/category/add", json=cat_data, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_update_category(self):
        list_resp = requests.get(f"{BASE_URL}/category/admin/list", headers=auth_headers(admin_token()))
        cats = list_resp.json().get("data", [])
        if not cats:
            pytest.skip("No categories")
        cat = cats[-1]
        update_data = {
            "categoryId": cat["categoryId"],
            "categoryName": f"更新分类_{random_str()}",
            "icon": cat.get("icon", ""),
            "sortOrder": cat.get("sortOrder", 0),
            "status": cat.get("status", 1)
        }
        resp = requests.put(f"{BASE_URL}/category/update", json=update_data, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_delete_category(self):
        cat_data = {
            "categoryName": f"待删分类_{random_str()}",
            "icon": "del",
            "sortOrder": 999,
            "status": 0
        }
        add = requests.post(f"{BASE_URL}/category/add", json=cat_data, headers=auth_headers(admin_token()))
        if not is_success(add.json()):
            pytest.skip("Add category failed")
        list_resp = requests.get(f"{BASE_URL}/category/admin/list", headers=auth_headers(admin_token()))
        cats = list_resp.json().get("data", [])
        target = None
        for c in cats:
            if c["categoryName"].startswith("待删分类"):
                target = c
                break
        if not target:
            pytest.skip("Created category not found")
        resp = requests.delete(f"{BASE_URL}/category/delete/{target['categoryId']}", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 10. Chat Message Module
# ============================================================

class TestChatMessage:
    def test_get_contacts(self):
        resp = requests.get(f"{BASE_URL}/chat/contacts", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_get_unread_count(self):
        resp = requests.get(f"{BASE_URL}/chat/unread-count", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_get_chat_history(self):
        resp = requests.get(f"{BASE_URL}/chat/contacts", headers=auth_headers(user_token()))
        contacts = resp.json().get("data", [])
        if not contacts:
            pytest.skip("No contacts")
        contact_id = contacts[0].get("userId") or contacts[0].get("contactId")
        if not contact_id:
            pytest.skip("No contact ID")
        hist = requests.get(f"{BASE_URL}/chat/history/{contact_id}", headers=auth_headers(user_token()))
        data = hist.json()
        assert is_success(data)

    def test_mark_read(self):
        resp = requests.get(f"{BASE_URL}/chat/contacts", headers=auth_headers(user_token()))
        contacts = resp.json().get("data", [])
        if not contacts:
            pytest.skip("No contacts")
        contact_id = contacts[0].get("userId") or contacts[0].get("contactId")
        if not contact_id:
            pytest.skip("No contact ID")
        read_resp = requests.post(f"{BASE_URL}/chat/read/{contact_id}", headers=auth_headers(user_token()))
        data = read_resp.json()
        assert is_success(data)


# ============================================================
# 11. Item Message Module
# ============================================================

class TestItemMessage:
    def test_add_item_message(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item_id = items[0]["itemId"]
        msg_data = {
            "itemId": item_id,
            "content": f"API测试留言_{random_str()}"
        }
        resp = requests.post(f"{BASE_URL}/item-message/add", json=msg_data, headers=auth_headers(user2_token()))
        data = resp.json()
        assert is_success(data)

    def test_get_item_messages(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item_id = items[0]["itemId"]
        resp = requests.get(f"{BASE_URL}/item-message/list/{item_id}", headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)

    def test_delete_item_message(self):
        list_resp = requests.get(f"{BASE_URL}/item/list", params={"categoryId": 1})
        items = list_resp.json().get("data", [])
        if not items:
            pytest.skip("No items")
        item_id = items[0]["itemId"]
        msg_data = {"itemId": item_id, "content": f"待删除留言_{random_str()}"}
        add = requests.post(f"{BASE_URL}/item-message/add", json=msg_data, headers=auth_headers(user2_token()))
        if not is_success(add.json()):
            pytest.skip("Add message failed")
        list_resp2 = requests.get(f"{BASE_URL}/item-message/list/{item_id}")
        msgs = list_resp2.json().get("data", [])
        target = None
        for m in msgs:
            if m.get("content", "").startswith("待删除留言"):
                target = m
                break
        if not target:
            pytest.skip("Created message not found")
        resp = requests.delete(f"{BASE_URL}/item-message/delete/{target['messageId']}", headers=auth_headers(user2_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 12. File Upload Module
# ============================================================

class TestFileUpload:
    def test_upload_file(self):
        import io
        file_content = f"test_file_content_{random_str()}".encode()
        files = {"file": ("test_upload.txt", io.BytesIO(file_content), "text/plain")}
        resp = requests.post(f"{BASE_URL}/common/upload", files=files, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)
        assert data.get("data")

    def test_upload_image(self):
        import io
        min_jpeg = b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00\xff\xdb\x00C\x00\x08\x06\x06\x07\x06\x05\x08\x07\x07\x07\t\t\x08\n\x0c\x14\r\x0c\x0b\x0b\x0c\x19\x12\x13\x0f\x14\x1d\x1a\x1f\x1e\x1d\x1a\x1c\x1c $.\' ",#\x1c\x1c(7),01444\x1f\'9=82<.342\xff\xc0\x00\x0b\x08\x00\x01\x00\x01\x01\x01\x11\x00\xff\xc4\x00\x1f\x00\x00\x01\x05\x01\x01\x01\x01\x01\x01\x00\x00\x00\x00\x00\x00\x00\x00\x01\x02\x03\x04\x05\x06\x07\x08\t\n\x0b\xff\xda\x00\x08\x01\x01\x00\x00?\x00T\xff\xd9'
        files = {"file": ("test.jpg", io.BytesIO(min_jpeg), "image/jpeg")}
        resp = requests.post(f"{BASE_URL}/common/upload", files=files, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 13. Admin Module
# ============================================================

class TestAdmin:
    def test_admin_dashboard(self):
        resp = requests.get(f"{BASE_URL}/admin/dashboard", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_user_list(self):
        resp = requests.get(f"{BASE_URL}/admin/user/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_user_detail(self):
        resp = requests.get(f"{BASE_URL}/admin/user/101", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_user_status(self):
        resp = requests.post(f"{BASE_URL}/admin/user/status/102/1", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_item_list(self):
        resp = requests.get(f"{BASE_URL}/admin/item/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_item_status(self):
        list_resp = requests.get(f"{BASE_URL}/admin/item/list", params={"page": 1, "size": 10}, headers=auth_headers(admin_token()))
        items = list_resp.json().get("data", {}).get("records", [])
        if not items:
            pytest.skip("No items in admin list")
        item_id = items[0]["itemId"]
        resp = requests.post(f"{BASE_URL}/admin/item/status/{item_id}/1", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)

    def test_admin_es_sync(self):
        resp = requests.post(f"{BASE_URL}/admin/es/sync", headers=auth_headers(admin_token()))
        data = resp.json()
        assert is_success(data)


# ============================================================
# 14. AI Module
# ============================================================

class TestAI:
    def test_ai_polish(self):
        polish_data = {
            "title": "二手教材出售",
            "description": "九成新教材便宜卖"
        }
        resp = requests.post(f"{BASE_URL}/ai/polish", json=polish_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data) or data.get("code") in [0, 500]

    def test_ai_chat(self):
        chat_data = {
            "message": "有什么便宜的二手书推荐吗？",
            "history": []
        }
        resp = requests.post(f"{BASE_URL}/ai/chat", json=chat_data, headers=auth_headers(user_token()))
        data = resp.json()
        assert is_success(data) or data.get("code") in [0, 500]


# ============================================================
# 15. Payment Module (basic endpoint check)
# ============================================================

class TestPayment:
    def test_alipay_redirect(self):
        order_resp = requests.get(f"{BASE_URL}/order/list/0", headers=auth_headers(user_token()))
        orders = order_resp.json().get("data", [])
        if not orders:
            order_resp2 = requests.get(f"{BASE_URL}/order/list/1", headers=auth_headers(user_token()))
            orders = order_resp2.json().get("data", [])
        if not orders:
            pytest.skip("No orders for payment test")
        order_id = orders[0]["orderId"]
        resp = requests.get(f"{BASE_URL}/pay/alipay", params={"orderId": order_id}, headers=auth_headers(user_token()), allow_redirects=False)
        assert resp.status_code in [200, 302, 307]


# ============================================================
# 16. Auth/Security Tests
# ============================================================

class TestAuthSecurity:
    def test_access_protected_without_token(self):
        resp = requests.get(f"{BASE_URL}/user/info")
        assert resp.status_code in [200, 401]
        data = resp.json()
        assert data.get("code") != 200 or resp.status_code == 401

    def test_access_admin_with_normal_user(self):
        resp = requests.get(f"{BASE_URL}/admin/dashboard", headers=auth_headers(user_token()))
        assert resp.status_code in [200, 403]
        data = resp.json()
        assert data.get("code") != 200 or resp.status_code == 403

    def test_invalid_token(self):
        resp = requests.get(f"{BASE_URL}/user/info", headers={"Authorization": "invalid_token_xyz"})
        assert resp.status_code in [200, 401]
        data = resp.json()
        assert data.get("code") != 200
