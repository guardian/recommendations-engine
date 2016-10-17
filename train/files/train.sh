#!/bin/bash -e

source /etc/environment

STAGE=$1

export HADOOP_USER_NAME=ubuntu
cd /opt/PredictionIO-0.9.5/
yarn_master=$(bin/discover-spark-cluster.sh $STAGE)
bin/update-yarn-config.sh $STAGE $yarn_master
bin/update-pio-config.sh $STAGE
cd /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1
time ../../bin/pio train -- --master yarn --deploy-mode client --driver-memory 1200M --verbose
cd /opt/PredictionIO-0.9.5/
bin/terminate-clusters.sh $STAGE
bin/set-asg-size.sh recommendations-train-$STAGE 0
