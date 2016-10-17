#!/bin/bash

STAGE=$1

cluseters=$(aws emr list-clusters \
	--region eu-west-1 \
	| jq ".Clusters[].Id" -r)

tmp_description=`mktemp -t test-XXXXXXXX`

for cluster_id in $active_clusters
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
    aws emr terminate-clusters --cluster-ids $cluster_id
fi

done

exit 0