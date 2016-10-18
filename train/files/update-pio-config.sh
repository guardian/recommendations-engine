#!/bin/bash -e

STAGE=$1

if [ "$STAGE" != "PROD" ] && [ "$STAGE" != "CODE" ]; then
    echo "Missing/invalid argument STAGE (valid stages are PROD or CODE)" 1>&2
    exit 1
fi

elasticsearch_host=$(bin/discover-elasticsearch-cluster.sh $STAGE | head -1)

sed /opt/PredictionIO-0.9.5/conf/pio-env.sh.template \
    -e "s/\${stage}/$STAGE/g" \
    -e "s/\${elasticsearch_host}/$elasticsearch_host/g" \
    -e "s/\${elasticsearch_port}/9300/g" \
    > /opt/PredictionIO-0.9.5/conf/pio-env.sh
