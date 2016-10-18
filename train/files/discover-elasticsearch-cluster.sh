#!/bin/bash -e

STAGE=$1

if [ "$STAGE" != "PROD" ] && [ "$STAGE" != "CODE" ]; then
    echo "Missing/invalid argument STAGE (valid stages are PROD or CODE)" 1>&2
    exit 1
fi

aws ec2 describe-instances \
    --query 'Reservations[*].Instances[*].[PrivateIpAddress]' \
    --filter Name=instance-state-name,Values=running \
        Name=tag:Stage,Values=$STAGE \
        Name=tag:Stack,Values=recommendations \
        Name=tag:App,Values=elasticsearch \
    --output text \
    --region eu-west-1