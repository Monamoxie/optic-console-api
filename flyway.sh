#!/bin/bash
# Flyway migration helper - loads .env automatically

set -a
source .env
set +a

./mvnw flyway:$1
