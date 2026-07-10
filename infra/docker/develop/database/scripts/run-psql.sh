#!/bin/bash

PGPASSWORD=$DB_PASSWORD psql -U $DB_USERNAME -d $DB_NAME -p $POSTGRES_PORT -h localhost
