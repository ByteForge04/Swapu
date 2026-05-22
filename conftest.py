import pytest
import requests
import time

BASE_URL = "http://localhost:8080"


@pytest.fixture(scope="session", autouse=True)
def pre_login():
    users = [
        ("user1", "zhangsan", "123456"),
        ("user2", "lisi", "123456"),
        ("admin", "admin", "123456"),
    ]
    tokens = {}
    for key, username, password in users:
        for attempt in range(5):
            resp = requests.post(f"{BASE_URL}/user/login", json={"username": username, "password": password})
            data = resp.json()
            if data.get("code") == 200 and data.get("data") and data["data"].get("token"):
                tokens[key] = data["data"]["token"]
                break
            time.sleep(2)
        time.sleep(1)
    import test_api_integration
    test_api_integration._tokens.update(tokens)
    if "admin" in tokens:
        test_api_integration._admin_token = tokens["admin"]
