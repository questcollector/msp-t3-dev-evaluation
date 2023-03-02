#!/bin/bash

# dir
rabbit_dir="./rabbit-data"
if [ ! -d "$rabbit_dir" ]; then mkdir ./rabbit-data
fi

db_dir="./db"
if [ ! -d "$db_dir" ]; then mkdir -p ./db/{data,initdb.d}
fi

rand_passwd=$(</dev/urandom tr -dc 'A-Za-z0-9@#$%&_+=' | head -c 16)

init_mongo="./db/initdb.d/init-mongo.js"
if [ ! -f "$init_mongo" ]; then
cat <<EOF > "$init_mongo"
db = new Mongo().getDB("students");

db.createCollection('message_data', { capped: false });

db.createUser({
    user: 'eval',
    pwd: '$rand_passwd',
    roles: [
        {
            role: 'readWrite',
            db: 'students',
        },
    ],
});
EOF
fi

if [ ! -f "./.env" ]; then
echo "Enter SLACK_USER_TOKEN: "
read -rs slack_user_token

cat <<EOF > ./.env
MONGO_HOST=mongo
MONGO_PORT=27017
MONGO_INITDB_ROOT_USERNAME=eval
MONGO_INITDB_ROOT_PASSWORD=$rand_passwd
MONGO_INITDB_DATABASE=students
MONGO_USER=eval
MONGO_PASSWORD=$rand_passwd
RABBITMQ_HOST=rabbit
SLACK_USER_TOKEN=$slack_user_token
TZ=Asia/Seoul
EOF
fi