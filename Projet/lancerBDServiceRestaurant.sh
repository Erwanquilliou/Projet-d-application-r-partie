#!/bin/bash

CONTAINER_NAME="mysql_container"
CONTAINER_ID=$(docker ps -a --filter "name=^${CONTAINER_NAME}$" --format "{{.ID}}")
#Vérifier si le conteneur existe
if [ -n "$CONTAINER_ID" ]; then
    echo "suppression"
    docker rm -f "$CONTAINER_ID"
fi

#Lancer Docker Compose
echo "Lancement de la base de la base de données mysql"
docker compose up -d
echo "Attente que MySQL écoute sur le port 3306..."
until docker exec mysql_container mysqladmin ping -h127.0.0.1 -uuser -p$1 --silent 2>/dev/null; do
  echo -n "."; sleep 1
done
echo "MySQL est prêt !"
echo "compilation"

javac -cp mysql-connector-j-9.3.0.jar -cp json-20131018.jar src/RMI/*.java
cd "$(dirname "$0")/src" || exit 1
echo "Lancer programme Restaurant"
java  -cp ".;../mysql-connector-j-9.3.0.jar;../json-20131018.jar" RMI.LancerServices
cd - > /dev/null

