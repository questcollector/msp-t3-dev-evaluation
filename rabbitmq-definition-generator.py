import os
import base64
import hashlib
import sys
import json

if (len(sys.argv) == 1):
    sys.argv.append("admin")
print(sys.argv)
admin_password = sys.argv[1]

def read_json(filename):
    with open(filename, "r") as file:
        return json.load(file)

def save_json(data, filename):
    with open(filename, "w", encoding='utf-8') as file:
        json.dump(data, file, indent=2)

def get_rabbit_password_hash(password):
    # generate 32 bit salt
    salt = os.urandom(4)  # 4 bytes = 32 bit
    # concatenate salt with utf-8 encoded password
    first = salt + password.encode('utf-8')
    # sha256 hash
    second = hashlib.sha256(first).digest()
    # concatenate salt again
    third = salt + second
    return base64.b64encode(third).decode()

if __name__ == '__main__':
    definition = read_json("rabbitmq-definition.json.tpl")
    definition["users"][0]["password_hash"] = get_rabbit_password_hash(admin_password)
    definition["users"][1]["password_hash"] = get_rabbit_password_hash("guest")
    save_json(definition, "rabbitmq-definition.json")