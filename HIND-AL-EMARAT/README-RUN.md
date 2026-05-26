# HIND AL EMARAT - Run Guide

## 1) Required Environment Variables

Set these before running in production:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_SEED_ENABLED=false`
- `SPRING_THYMELEAF_CACHE=true`
- `APP_LOG_LEVEL=INFO`
- `FILE_UPLOAD_DIR=/absolute/path/to/uploads/products`

Optional for one-time bootstrap admin/user seeding:

- `APP_SEED_ADMIN_EMAIL`
- `APP_SEED_ADMIN_PASSWORD`
- `APP_SEED_USER_EMAIL`
- `APP_SEED_USER_PASSWORD`

## 2) Build

```bash
mvn clean package
```

## 3) Run Executable Jar

```bash
java -jar target/hind-al-emarat-1.0.0.jar
```

## 4) Open

Application URL:

- `http://localhost:8080`
