# Evaluation 서비스

## Spring webflux with Kotlin

## 로컬에서 구동 시

1. docker-compose-dev.yml로 rabbitmq, mongodb 실행
2. rabbitmq metadata 적용
3. `SLACK_USER_TOKEN` 환경 변수 추가
4. `--spring.profiles.active=dev` 옵션으로 실행

## 로컬에서 slack service 테스트 시

1. `SLACK_USER_TOKEN` 환경 변수 추가
2. `SLACK_USER_ID` 환경 변수 추가