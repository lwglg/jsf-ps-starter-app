#!/bin/sh

export CATALINA_OPTS="-Ddb.hostname=${DB_HOSTNAME} -Ddb.username=${DB_USERNAME} -Ddb.password=${DB_PASSWORD} -Ddb.port=${DB_PORT} -Ddb.name=${DB_NAME} -Xms512m -Xmx1024m"
export JVM_ROUTE="tomcat_node_1"
