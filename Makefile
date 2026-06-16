.PHONY: help run stop restart status logs
.DEFAULT_GOAL := help

help:
	@echo "Доступные команды:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

run: ## Запустить все контейнеры со сборкой образов в фоне
	sudo docker compose -f docker-compose.yml up -d --build

stop: ## Остановить контейнеры (с сохранением данных)
	sudo docker compose -f docker-compose.yml down

restart: ## Полный перезапуск: сброс контейнеров и БД, чистый запуск с нуля
	sudo docker compose -f docker-compose.yml down -v
	sudo docker compose -f docker-compose.yml up -d --build

status: ## Проверить статус запущенных контейнеров
	sudo docker ps

logs: ## Посмотреть логи бэкенд-приложения в реальном времени
	sudo docker compose logs -f finance-tracker