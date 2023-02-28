# Evaluation 서비스

## Spring webflux with Kotlin

## 로컬에서 구동 시

1. gradlew build
2. rabbitmq 구성
   - rabbitmq:management 이미지
   - Administrator 권한의 admin/admin 계정 생성 필요
3. mongodb 구성
   - Docker container 환경변수
   - MONGO_INITDB_ROOT_USERNAME=eval
   - MONGO_INITDB_ROOT_PASSWORD=random
   - MONGO_INITDB_ROOT_DATABASE=students
   - port 27017
4. '--spring.profiles.active=dev' 옵션으로 실행
