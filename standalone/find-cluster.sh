#!/bin/bash

STAGE=$1

active_clusters=$(aws emr list-clusters \
 	--cluster-states RUNNING \
 	--cluster-states WAITING \
	--region eu-west-1 \
	--profile mobile \
	| jq ".Clusters[].Id" -r)

tmp_description=`mktemp -t test-XXXXXXXX`

for cluster_id in $active_clusters
do

aws emr describe-cluster \
	--cluster-id $cluster_id \
	--region eu-west-1 \
	--profile mobile \
	> $tmp_description

master_public_dns=$(cat $tmp_description | jq ".Cluster.MasterPublicDnsName" -r)

cat $tmp_description | \
	jq ".Cluster.Tags | from_entries | .App == \"spark\" and .Stack == \"recommendations\" and .Stage == \"$STAGE\"" -e > /dev/null

if [ $? -eq 0 ]; then
	echo $master_public_dns
	rm $tmp_description
	exit 0
fi

done

rm $tmp_description
echo "No valid clusters found" 1>&2
exit 1