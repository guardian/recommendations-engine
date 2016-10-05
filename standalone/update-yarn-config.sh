#!/bin/bash -e

yarn_master=$(./find-cluster.sh)

sed "s/\${yarn-master}/$yarn_master/" /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/core-site.xml
sed "s/\${yarn-master}/$yarn_master/" /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml.template > /opt/PredictionIO-0.9.5/conf/yarn/yarn-site.xml
