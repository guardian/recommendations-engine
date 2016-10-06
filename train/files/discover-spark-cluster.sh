#!/bin/bash

STAGE=$1

active_clusters=$(aws emr list-clusters \
 	--cluster-states RUNNING \
 	--cluster-states WAITING \
	--region eu-west-1 \
	| jq ".Clusters[].Id" -r)

tmp_description=`mktemp -t test-XXXXXXXX`

for cluster_id in $active_clusters
do

aws emr describe-cluster \
	--cluster-id $cluster_id \
	--region eu-west-1 \
	> $tmp_description

master_public_dns=$(cat $tmp_description | jq ".Cluster.MasterPublicDnsName" -r)

correct_cluster=$(cat $tmp_description | \
    sed -e 's/Key/key/g' -e 's/Value/value/g' | \
	jq ".Cluster.Tags | from_entries | .App == \"spark\" and .Stack == \"recommendations\" and .Stage == \"$STAGE\"")

if [ "$correct_cluster" = "true" ]; then
	echo $master_public_dns
	rm $tmp_description
	exit 0
fi

done

rm $tmp_description
echo "No valid clusters found" 1>&2
exit 1