#!/bin/bash -e

$STAGE=$1

export HADOOP_USER_NAME=ubuntu
cd /opt/PredictionIO-0.9.5/
bin/update-yarn-config.sh $STAGE
bin/update-pio-config.sh $STAGE
cd /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1
time ../../bin/pio train -- --master yarn --deploy-mode client --driver-memory 1200M --verbose