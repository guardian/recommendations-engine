#!/bin/bash -e

STAGE=$1

elasticsearch_host=$(bin/discover-elasticsearch-cluster.sh $STAGE | head -1)

sed /opt/PredictionIO-0.9.5/conf/pio-env.sh.template \
    -e "s/\${stage}/$STAGE/g" \
    -e "s/\${elasticsearch_host}/$elasticsearch_host/g" \
    -e "s/\${elasticsearch_port}/9300/g" \
    > /opt/PredictionIO-0.9.5/conf/pio-env.sh
