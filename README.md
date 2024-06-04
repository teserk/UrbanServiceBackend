# UrbanServicesBackend

UrbanServicesBackend - это backend-приложение, разработанное для обеспечения различных сервисов, связанных с городской инфраструктурой. Проект представляет собой набор микросервисов, каждый из которых обеспечивает определенную функциональность.

## Микросервисы

1. **Auth-Service**: Сервис авторизации, который обеспечивает аутентификацию пользователей и управление доступом.
2. **Routing-Service**: Сервис добавления домов и улиц и поиска маршрутов 
   между домами. Используется алгоритм A-star.
3. **Photo-Service**: Сервис фотографий, предоставляющий возможность 
   загрузки и управления фотографиями до 10 мегабайт. Реализовано с помощью 
   ZStreams без буфферизации.

Подробное OpenAPI сервисов есть в папке api

## Установка и запуск

1. Клонируйте репозиторий: `git clone https://github.com/teserk/UrbanServiceBackend.git`
2. Перейдите в директорию docker-compose: `cd 
   UrbanServicesBackend/docker-compose`
3. Запустите сервис: `docker compose up --build`

## Технологии

Проект разработан с использованием следующих технологий:

- **Язык программирования**: Scala
- **Фреймворк**: ZIO, ZIO-HTTP, ZIO-SQL
- **База данных**: PostgreSQL
- **Контейнеризация**: Docker
- **Тесты**: ZIO Test
