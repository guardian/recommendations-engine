#!/bin/bash -e

STAGE=$1

if [ "$STAGE" != "PROD" ] && [ "$STAGE" != "CODE" ]; then
    echo "Missing/invalid argument STAGE (valid stages are PROD or CODE)" 1>&2
    exit 1
fi

elasticsearch_host=$(bin/discover-elasticsearch-cluster.sh $STAGE | head -1)

if [ ! -e "/opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1/engine.json.template" ]; then
    cp /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1/engine.json /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1/engine.json.template
fi

sed /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1/engine.json.template \
    -e "s/\${elasticsearch_host}/$elasticsearch_host/g" \
    > /opt/PredictionIO-0.9.5/engines/MyGuardianTestEngine-1/engine.json