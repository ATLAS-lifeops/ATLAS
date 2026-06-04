.PHONY: start up down logs restart status clean

start:
	./scripts/start.sh

up:
	docker compose up --build -d

down:
	docker compose down

logs:
	docker compose logs -f atlas-app

restart:
	docker compose restart atlas-app

status:
	docker compose ps

clean:
	docker compose down
