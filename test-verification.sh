#!/bin/bash
API_BASE="http://localhost:9090/api"

# 1. Login as Influencer
INF_RES=$(curl -s -X POST "$API_BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"influencer@example.com", "password":"password"}')

INF_TOKEN=$(echo $INF_RES | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$INF_TOKEN" ]; then
  echo "Failed to login as influencer: $INF_RES"
  exit 1
fi
echo "Influencer logged in."

# 2. Request Verification
REQ_RES=$(curl -s -X POST "$API_BASE/users/me/verification/request" \
  -H "Authorization: Bearer $INF_TOKEN" \
  -H "Content-Type: application/json")
echo "Request Verification Response: $REQ_RES"

# 3. Check verification status
STAT_RES=$(curl -s -X GET "$API_BASE/users/me/verification/status" \
  -H "Authorization: Bearer $INF_TOKEN" \
  -H "Content-Type: application/json")
echo "Status Verification Response: $STAT_RES"

# 4. Login as Admin
ADMIN_RES=$(curl -s -X POST "$API_BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com", "password":"password"}')

ADMIN_TOKEN=$(echo $ADMIN_RES | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to login as admin: $ADMIN_RES"
  exit 1
fi
echo "Admin logged in."

# 5. List Verification Requests
LIST_RES=$(curl -s -X GET "$API_BASE/admin/verification-requests" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json")
echo "List Verification Requests as Admin: $LIST_RES"

