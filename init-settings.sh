#!/bin/bash

# dir
rabbit_dir="./rabbit-data"
if [ ! -d "$rabbit_dir" ]; then mkdir ./rabbit-data
fi

db_dir="./db"
if [ ! -d "$db_dir" ]; then mkdir -p ./db/{data,initdb.d}
fi

rand_passwd=$(base64 /dev/urandom | head -c32)

init_mongo="./db/initdb.d/init-mongo.js"
if [ ! -f "$init_mongo" ]; then
cat <<EOF > "$init_mongo"
db = new Mongo().getDB("students");

db.createCollection('message_data', {
   validator: { \$jsonSchema: {
      bsonType: "object",
      properties: {
         _id: {
            bsonType: "binData",
            description: "must be a UUID string and is required"
         }
      }
   }},
   validationAction: "error"
});

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
echo "Enter SLACK_BOT_TOKEN: "
read -rs slack_bot_token

erlang_cookie_random=$(base64 /dev/urandom | head -c32)

cat <<EOF > ./.env
MONGO_HOST=mongo
MONGO_PORT=27017
MONGO_INITDB_ROOT_USERNAME=eval
MONGO_INITDB_ROOT_PASSWORD=$rand_passwd
MONGO_INITDB_DATABASE=students
MONGO_USER=eval
MONGO_PASSWORD=$rand_passwd
RABBITMQ_HOST=rabbit
RABBITMQ_ERLANG_COOKIE=$erlang_cookie_random
SLACK_BOT_TOKEN=$slack_bot_token
TZ=Asia/Seoul
EOF
fi