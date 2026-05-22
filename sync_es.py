import requests
import json

# 1. 登录获取 Token (假设有一个管理员账号，如果没有，可能需要先注册或直接操作数据库提升权限)
# 这里假设 admin/123456 是管理员 (根据 AdminController 逻辑，通常会有预设管理员，或者我们先用普通用户登录试试，如果后端鉴权没做好可能也能调用)
# 如果没有管理员账号，我可能需要先查看数据库或者注册一个。
# 先尝试用之前的 test/123456 登录，如果权限不足再想办法。

login_url = 'http://localhost:8080/user/login'
login_data = {
    'username': 'admin', 
    'password': '123456' # 使用默认密码
}

try:
    # 尝试登录
    print("Attempting to login...")
    res = requests.post(login_url, json=login_data)
    res_json = res.json()
    
    token = ""
    if res_json['code'] == 200:
        token = res_json['data']['token']
        print("Login successful, token obtained.")
    else:
        print(f"Login failed: {res_json['msg']}")
        # 如果登录失败，尝试注册一个新用户并提升为管理员（需要数据库操作，python脚本里不好做，先看结果）
        exit(1)

    # 2. 调用同步接口
    sync_url = 'http://localhost:8080/admin/es/sync'
    headers = {
        'Authorization': token
    }
    
    print("Triggering ES sync...")
    sync_res = requests.post(sync_url, headers=headers)
    print(json.dumps(sync_res.json(), indent=2, ensure_ascii=False))

except Exception as e:
    print(f"Error: {e}")
