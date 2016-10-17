#!/bin/bash -e

STAGE=$1
YARN_MASTER=$2

sed "s/\${yarn-master}/$YARN_MASTER/g" /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml
sed "s/\${yarn-master}/$YARN_MASTER/g" /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml
