#!/bin/bash

STAGE=$1

if [ "$STAGE" != "PROD" ] && [ "$STAGE" != "CODE" ]; then
    echo "Missing/invalid argument STAGE (valid stages are PROD or CODE)" 1>&2
    exit 1
fi

clusters=$(aws emr list-clusters --region eu-west-1 \
    | jq ".Clusters[] |
        select(.Status.State==\"WAITING\" or
        .Status.State == \"RUNNING\" or
        .Status.State == \"STARTING\" or
        .Status.State == \"BOOTSTRAPPING\") | .Id" -r)

tmp_description=`mktemp -t test-XXXXXXXX`

for cluster_id in $clusters
do

aws emr describe-cluster \
	--cluster-id $cluster_id \
	--region eu-west-1 \
	> $tmp_description

correct_cluster=$(cat $tmp_description | \
    sed -e 's/Key/key/g' -e 's/Value/value/g' | \
	jq ".Cluster.Tags | from_entries | .App == \"spark\" and .Stack == \"recommendations\" and .Stage == \"$STAGE\"")

if [ "$correct_cluster" = "true" ]; then
    echo "Terminating cluster $cluster_id"
    aws emr terminate-clusters --cluster-ids $cluster_id --region eu-west-1
fi

done

exit 0