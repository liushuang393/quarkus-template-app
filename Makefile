# Quarkus 認証システム Makefile

.PHONY: help dev prod build test clean docker-build docker-run docker-stop

# デフォルトターゲット
help:
	@echo "Quarkus 認証システム - 利用可能なコマンド:"
	@echo ""
	@echo "開発環境:"
	@echo "  make dev          - 開発モードで起動 (H2データベース)"
	@echo "  make dev-db       - 開発用PostgreSQLを起動"
	@echo "  make dev-full     - 開発用フルスタック起動"
	@echo ""
	@echo "本番環境:"
	@echo "  make prod         - 本番モードで起動"
	@echo "  make prod-db      - 本番用PostgreSQLを起動"
	@echo ""
	@echo "ビルド:"
	@echo "  make build        - JVMモードでビルド"
	@echo "  make build-native - ネイティブモードでビルド"
	@echo "  make test         - テスト実行"
	@echo "  make clean        - クリーンアップ"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build - Dockerイメージビルド"
	@echo "  make docker-run   - Dockerコンテナ起動"
	@echo "  make docker-stop  - Dockerコンテナ停止"
	@echo ""

# 開発環境
dev:
	./mvnw quarkus:dev -Pdev

dev-db:
	docker-compose -f docker-compose.dev.yml up -d postgres-dev pgadmin-dev

dev-full:
	docker-compose -f docker-compose.dev.yml up -d

# 本番環境
prod:
	./mvnw quarkus:dev -Pprod

prod-db:
	docker-compose up -d postgres pgadmin

# ビルド
build:
	./mvnw clean package -Pdev

build-native:
	./mvnw clean package -Dnative -Pdev

# テスト
test:
	./mvnw test -Pdev

test-integration:
	./mvnw verify -Pdev

# クリーンアップ
clean:
	./mvnw clean
	docker-compose down -v
	docker-compose -f docker-compose.dev.yml down -v

# Docker操作
docker-build:
	./mvnw package
	docker build -f docker/Dockerfile.jvm -t quarkus-auth:latest .

docker-build-native:
	./mvnw package -Dnative
	docker build -f docker/Dockerfile.native -t quarkus-auth:native .

docker-run:
	docker-compose up -d

docker-stop:
	docker-compose down

# データベース操作
db-reset:
	docker-compose down -v postgres
	docker-compose up -d postgres

# ログ確認
logs:
	docker-compose logs -f quarkus-app

logs-db:
	docker-compose logs -f postgres
