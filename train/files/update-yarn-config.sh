#!/bin/bash -e

STAGE=$1

yarn_master=$(./discover-spark-cluster.sh $STAGE)

sed "s/\${yarn-master}/$yarn_master/g" /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml
sed "s/\${yarn-master}/$yarn_master/g" /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml
