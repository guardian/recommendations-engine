#!/bin/bash -e

STAGE=$1

aws ec2 describe-instances \
    --query 'Reservations[*].Instances[*].[PrivateIpAddress]' \
    --filter Name=instance-state-name,Values=running \
        Name=tag:Stage,Values=$STAGE \
        Name=tag:Stack,Values=recommendations \
        Name=tag:App,Values=elasticsearch \
    --output text \
    --region eu-west-1