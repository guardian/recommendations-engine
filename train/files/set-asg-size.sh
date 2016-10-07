#!/bin/bash

ASG=$1
SIZE=$2

if [ "$ASG" = "" ]; then
    echo "Autoscaling group name must be specified" 1>&2
    exit 1
fi

ASGNAME=`aws autoscaling describe-auto-scaling-groups --region eu-west-1 | grep "AutoScalingGroupName.*$ASG" | sed -e 's/^.*: *"\([^"]*\)".*$/\1/'`
aws autoscaling update-auto-scaling-group --auto-scaling-group-name $ASGNAME --desired-capacity $SIZE --region eu-west-1
