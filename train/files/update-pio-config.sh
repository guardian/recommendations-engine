#!/bin/bash -e

STAGE=$1

elasticsearch_host=$(./discover-elasticsearch-cluster.sh $STAGE)

sed /opt/PredictionIO-0.9.5/conf/pio-env.sh.template \
    -e "s/\${stage}/$STAGE/g" \
    -e "s/\${elasticsearch_host}/$elasticsearch_host/g" \
    -e "s/\${elasticsearch_port}/9300/g" \
    > /opt/PredictionIO-0.9.5/conf/pio-env.sh
