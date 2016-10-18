#!/bin/bash

STAGE=$1

active_clusters=$(aws emr list-clusters --region eu-west-1 \
    | jq ".Clusters[] |
        select(.Status.State==\"WAITING\" or
        .Status.State == \"RUNNING\" or
        .Status.State == \"STARTING\" or
        .Status.State == \"BOOTSTRAPPING\") | .Id" -r)

tmp_description=`mktemp -t test-XXXXXXXX`

selected_cluster_id="none"

# Find active cluster if available
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
    echo "Found existing spark cluster $cluster_id" 1>&2
    selected_cluster_id="$cluster_id"
    break
fi

done

# Create a new cluster if we couldn't find one
if [ "$selected_cluster_id" = "none" ]; then
    echo "No existing spark cluster found.  Creating a new one" 1>&2
    selected_cluster_id=$(bin/create-cluster.sh | jq ".ClusterId" -r)
fi

echo "Waiting for spark cluster $selected_cluster_id to become available"  1>&2

for i in {0..60}
do

aws emr describe-cluster \
	--cluster-id $selected_cluster_id \
	--region eu-west-1 \
	> $tmp_description

cluster_state=$(cat $tmp_description | jq ".Cluster.Status.State" -r)

if [ "$cluster_state" = "RUNNING" ] || [ "$cluster_state" = "WAITING" ]; then
  master_public_dns=$(cat $tmp_description | jq ".Cluster.MasterPublicDnsName" -r)
  echo "$master_public_dns"
  rm $tmp_description
  exit 0
fi

sleep 30

done

echo "Cluster not ready after 30 minutes, giving up" 1>&2
rm $tmp_description
exit 1