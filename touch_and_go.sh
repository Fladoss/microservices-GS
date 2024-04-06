set -e

docker compose down

docker image prune --force

for service in product-service order-service inventory-service discovery-server api-gateway notification-service;
do
    (cd "$service" && mvn clean package -DskipTests)
done

docker-compose build --no-cache
docker compose up --build -d