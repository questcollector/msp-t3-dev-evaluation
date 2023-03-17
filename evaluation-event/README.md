# Evaluation 서비스

## Spring webflux with Kotlin

## Slack Bot 만들기 

아래 문서를 참조하여 slack bot을 생성한 다음 slack bot user token을 사용합니다.

🔗 https://tall-fuel-e5e.notion.site/4-Slack-bot-fedf51dd032f4fe895d73443847115fc

## 로컬에서 구동 시

1. docker-compose-dev.yml로 rabbitmq, mongodb 실행
2. rabbitmq metadata 적용
3. `SLACK_BOT_TOKEN` 환경 변수 추가
4. `--spring.profiles.active=dev` 옵션으로 실행

## 로컬에서 slack service 테스트 케이스 실행 시

1. `SLACK_BOT_TOKEN` 환경 변수 추가
2. `SLACK_USER_ID` 환경 변수 추가
