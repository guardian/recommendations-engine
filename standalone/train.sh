#!/bin/bash

export HADOOP_USER_NAME=ec2-user
cd /opt/PredictionIO-0.9.5/
./update-yarn-config.sh
cd /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1
time ../../bin/pio train -- --master spark://master.spark.recommendations.prod.internal:7077 --driver-memory 1200M --verbose