# 실습 과제 평가용 스택

## instance userdata.txt
- ubuntu ec2
- docker, docker-compose 설치
- ecr image 권한

## init-settings.sh
환경 변수, 초기 상태에 대한 설정파일

## docker-compose.yml
docker-compose 정의 파일

## rabbitmq 라우팅 키 설정
아래의 바인드에 대해 삭제 후 라우팅 키를 지정하지 않음

|exchange|queue|
|--|--|
|campaignAddedEvent|campaignAddedEvent.notification|
|notificationSuccessEvent|notificationSuceessEvent.marketing|
|notificationFailedEvent|notificationFailedEvent.marketing|
