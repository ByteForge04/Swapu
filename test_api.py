import requests
import json

try:
    response = requests.get('http://localhost:8080/announcement/list?page=1&size=10')
    data = response.json()
    print(json.dumps(data, indent=2, ensure_ascii=False))
except Exception as e:
    print(e)
