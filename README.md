# 실습 과제 평가용 스택

## instance userdata.txt
- ubuntu ec2
- awscli, docker, docker-compose 설치
- ecr image 권한(이미지는 아직 공유 안되어서 직접 빌드해야함)
  - AmazonEC2ContainerRegistryReadOnly

## init-settings.sh
환경 변수, 초기 상태에 대한 설정파일

## docker-compose.yml
docker-compose 정의 파일

## rabbitmq 라우팅 키 설정
rabbitmq 설정 공유파일 제공

## 로컬에서 실행

1. 기본 실행
   - 디렉토리 구성
      - `./db/data/`
      - `./rabbit-data/`
2. evaluation-api 실행
   - Program argument: --spring.profiles.active=dev
3. evaluation-event 실행
   - Program argument: --spring.profiles.active=dev
   - env var: SLACK_USER_TOKEN