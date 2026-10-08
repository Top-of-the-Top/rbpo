# Vedu

Таск-трекер для небольших команд. Автор создаёт задачу и назначает исполнителя, исполнитель меняет статус по ходу работы, автор принимает результат или возвращает на доработку. Доска с фиксированными статусами, права на каждое действие проверяются на сервере.

Подробнее: паспорт продукта [PROJECT.md](PROJECT.md), требования безопасности [docs/SECURITY_REQUIREMENTS.md](docs/SECURITY_REQUIREMENTS.md), модель угроз [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md), проектные решения [docs/DESIGN_DECISIONS.md](docs/DESIGN_DECISIONS.md).

## Запуск

Нужен Docker и Docker Compose.

```bash
cp .env.example .env   # заполнить JWT_SECRET, EMAIL_ENCRYPTION_KEY, EMAIL_IV_KEY и пароли БД/Redis/RabbitMQ
docker compose up --build
```

API поднимается на `http://localhost:8080`. Проверка работоспособности и остальные детали запуска: [PROJECT.md §10](PROJECT.md#10-локальный-запуск).
