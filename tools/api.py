import requests

payload = {
    'login': 'hp@cleantalk.org',
    'password': 'qwerty123',
    'app_device_token': 'mytoken',
    'app_sender_id': 'senderid'
}
r = requests.post("https://cleantalk.org/my/session?app_mode=1", data=payload)
if r.status_code == 200:
    sessionId = r.json()['app_session_id']

# print(sessionId)

payload = {
    'app_session_id': sessionId
}
r = requests.post("https://cleantalk.org/my/main?app_mode=1&service_page=4", data=payload)
if r.status_code == 200:
    print(r.json())
    # print(r.headers)

payload = {
    "service_id": 192173,
    'app_session_id': sessionId,
    "start_from": 1480359600,
    "allow": 0
}

# r = requests.post("https://cleantalk.org/my/show_requests?app_mode=1", data=payload)
# if r.status_code == 200:
#     print(r.json())
    # print(r.headers)

