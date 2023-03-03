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
2. RabbitMQ와 MongoDB는 Docker compose로 구동할 수 있음
```shell
docker-compose -f docker-compose-dev.yml up -d
```
3. Rabbitmq config
   - JSON설정 파일을 rabbitmq management에서 (링크 http://localhost:15672 ) </br>
     overview > Import definitions 에 업로드하여 Upload broker definitions 버튼을 클릭합니다.
     ![](rabbitmq-config/image.png)</br>
   - 적용 후 logout 하고 admin/admin 계정으로 로그인합니다.
4. evaluation-api 실행
   - 실행 시 Edit configuration을 하여 Program arguments 수정
   - Program argument: ```--spring.profiles.active=dev```
5. evaluation-event 실행
   - 실행 시 Edit configuration을 하여 Program arguments 수정
   - Program argument: ```--spring.profiles.active=dev```
   - env var: SLACK_USER_TOKEN
   - SLACK_USER_TOKEN: