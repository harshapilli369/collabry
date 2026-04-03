import urllib.request
import json
import time

base_url = "http://localhost:9090/api"

def register(email, password, role):
    req = urllib.request.Request(f"{base_url}/auth/register", data=json.dumps({"email":email, "password":password, "role":role}).encode("utf-8"), headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req) as response:
            return True
    except Exception as e:
        print(f"Register failed for {email}: {e}")
        return False

def login(email, password):
    req = urllib.request.Request(f"{base_url}/auth/login", data=json.dumps({"email":email, "password":password}).encode("utf-8"), headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode())["token"]
    except Exception as e:
        print(f"Login failed for {email}: {e}")
        return None

def request_verification(token):
    req = urllib.request.Request(f"{base_url}/users/me/verification/request", data=b'', headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req) as response:
            print("Request Verification:", response.read().decode())
    except urllib.error.HTTPError as e:
        print("Request Verification failed:", e.read().decode())

def list_requests(token):
    req = urllib.request.Request(f"{base_url}/admin/verification-requests", headers={"Authorization": f"Bearer {token}"})
    try:
        with urllib.request.urlopen(req) as response:
            print("Admin List:", response.read().decode())
    except urllib.error.HTTPError as e:
        print("Admin List failed:", e.read().decode())

timestamp = int(time.time())
email = f"testinf{timestamp}@example.com"
register(email, "password", "INFLUENCER")
inf_token = login(email, "password")
if inf_token:
    print(f"{email} logged in")
    request_verification(inf_token)

admin_token = login("admin@collabry.com", "password123")
if admin_token:
    print("Admin logged in")
    list_requests(admin_token)
