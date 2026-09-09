#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# check-microservices-health.sh
# Vérifie la santé de l'architecture FitConnect (Docker Compose)
# Usage : ./check-microservices-health.sh
# ============================================================

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Services attendus et leurs ports exposés
declare -A SERVICES=(
  [eureka-server]=8761
  [config-server]=8888
  [api-gateway]=8080
  [class-service]=8091
  [booking-service]=8092
  [payment-service]=8093
  [notification-service]=8094
)

FAIL=0

echo "=============================================="
echo "  Vérification de la santé des microservices"
echo "=============================================="

# --- 1. Vérifier que Docker est lancé ---
echo ""
echo "[1/3] Vérification de Docker..."
if ! docker info >/dev/null 2>&1; then
  echo -e "${RED}✗ Docker n'est pas lancé. Démarrez Docker Desktop puis relancez ce script.${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Docker est lancé${NC}"

# --- 2. Vérifier que les conteneurs sont up ---
echo ""
echo "[2/3] Vérification des conteneurs Docker Compose..."
if ! docker compose ps >/dev/null 2>&1; then
  echo -e "${RED}✗ docker compose ps a échoué. Vérifiez que vous êtes à la racine du projet.${NC}"
  exit 1
fi

for name in "${!SERVICES[@]}"; do
  status=$(docker compose ps --format '{{.Service}} {{.State}}' 2>/dev/null | awk -v s="$name" '$1==s {print $2}')
  if [ "$status" = "running" ]; then
    echo -e "${GREEN}✓ $name : running${NC}"
  else
    echo -e "${RED}✗ $name : ${status:-absent}${NC}"
    FAIL=1
  fi
done

# --- 3. Vérifier les healthchecks HTTP ---
echo ""
echo "[3/3] Vérification des endpoints /actuator/health..."
for name in "${!SERVICES[@]}"; do
  port="${SERVICES[$name]}"
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "http://localhost:${port}/actuator/health" 2>/dev/null || echo "000")
  if [ "$code" = "200" ]; then
    echo -e "${GREEN}✓ $name (:${port}) : HTTP $code${NC}"
  else
    echo -e "${RED}✗ $name (:${port}) : HTTP $code${NC}"
    FAIL=1
  fi
done

echo ""
if [ "$FAIL" -eq 0 ]; then
  echo -e "${GREEN}=============================================="
  echo -e "  Tous les services sont sains ✓"
  echo -e "==============================================${NC}"
  exit 0
else
  echo -e "${RED}=============================================="
  echo -e "  Des services sont en erreur ✗"
  echo -e "  Lancez : docker compose up --build"
  echo -e "==============================================${NC}"
  exit 1
fi
