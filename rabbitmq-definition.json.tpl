{
  "rabbit_version": "3.11.8",
  "rabbitmq_version": "3.11.8",
  "product_name": "RabbitMQ",
  "product_version": "3.11.8",
  "users": [
    {
      "name": "admin",
      "password_hash": "${admin_password}",
      "hashing_algorithm": "rabbit_password_hashing_sha256",
      "tags": [
        "administrator"
      ],
      "limits": {}
    },
    {
      "name": "guest",
      "password_hash": "${guest_password}",
      "hashing_algorithm": "rabbit_password_hashing_sha256",
      "tags": [],
      "limits": {}
    }
  ],
  "vhosts": [
    {
      "name": "/"
    }
  ],
  "permissions": [
    {
      "user": "admin",
      "vhost": "/",
      "configure": ".*",
      "write": ".*",
      "read": ".*"
    },
    {
      "user": "guest",
      "vhost": "/",
      "configure": "^((?!marketing).)*$",
      "write": "^((?!marketing).)*$",
      "read": "^((?!marketing).)*$"
    }
  ],
  "topic_permissions": [
    {
      "user": "guest",
      "vhost": "/",
      "exchange": "notificationFailedEvent",
      "write": "^((?!marketing).)*$",
      "read": "^((?!marketing).)*$"
    },
    {
      "user": "guest",
      "vhost": "/",
      "exchange": "notificationSuccessEvent",
      "write": "^((?!marketing).)*$",
      "read": "^((?!marketing).)*$"
    },
    {
      "user": "guest",
      "vhost": "/",
      "exchange": "campaignAddedEvent",
      "write": "^((?!notification).)*$",
      "read": "^((?!notification).)*$"
    }
  ],
  "parameters": [],
  "global_parameters": [
    {
      "name": "internal_cluster_id",
      "value": "rabbitmq-cluster-id-CsixpEzcWgHVKyIn4X7ZDQ"
    }
  ],
  "policies": [],
  "queues": [
    {
      "name": "notificationFailedEvent.marketing",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "campaignAddedEvent.evaluation.dlq",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "notificationSuccessEvent.marketing",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "campaignAddedEvent.notification",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "campaignAddedEvent.evaluation",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    }
  ],
  "exchanges": [
    {
      "name": "DLX",
      "vhost": "/",
      "type": "direct",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "campaignAddedEvent",
      "vhost": "/",
      "type": "topic",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notificationFailedEvent",
      "vhost": "/",
      "type": "topic",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notificationSuccessEvent",
      "vhost": "/",
      "type": "topic",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    }
  ],
  "bindings": [
    {
      "source": "DLX",
      "vhost": "/",
      "destination": "campaignAddedEvent.evaluation.dlq",
      "destination_type": "queue",
      "routing_key": "campaignAddedEvent.evaluation",
      "arguments": {}
    },
    {
      "source": "campaignAddedEvent",
      "vhost": "/",
      "destination": "campaignAddedEvent.notification",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "campaignAddedEvent",
      "vhost": "/",
      "destination": "campaignAddedEvent.evaluation",
      "destination_type": "queue",
      "routing_key": "#",
      "arguments": {}
    },
    {
      "source": "notificationFailedEvent",
      "vhost": "/",
      "destination": "notificationFailedEvent.marketing",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notificationSuccessEvent",
      "vhost": "/",
      "destination": "notificationSuccessEvent.marketing",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    }
  ]
}