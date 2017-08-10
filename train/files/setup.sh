#!/bin/bash

instanceid=`curl -s http://169.254.169.254/latest/meta-data/instance-id`
region=`curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's/.$//'`
stagetag=`aws ec2 describe-tags --filters "Name=resource-id,Values=$instanceid" "Name=resource-type,Values=instance" "Name=key,Values=Stage" --region $region | grep -oP "(?<=\"Value\": \")[^\"]+"`

apt-get install -y jq

# Fetch packages
mkdir -p /root/files/packages
cd /root/files/packages
aws s3 cp s3://recommendations-dist/software/PredictionIO-0.9.5.tar.gz . --region eu-west-1
wget -nv http://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz
wget -nv https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.4.4.tar.gz
aws s3 cp s3://recommendations-dist/software/hbase-1.0.3-bin.tar.gz . --region eu-west-1

cd /opt/

# Install prediction.io
tar zxf /root/files/packages/PredictionIO-0.9.5.tar.gz

cp /root/files/pio-env.sh.template PredictionIO-0.9.5/conf/

mkdir PredictionIO-0.9.5/conf/hadoop
cp /root/files/hadoop-conf/core-site.xml PredictionIO-0.9.5/conf/hadoop/
cp /root/files/hadoop-conf/mapred-site.xml PredictionIO-0.9.5/conf/hadoop/

sed -i "s/<-hdfs-namenode->/master-0.recommendations.$stagetag.internal/g" PredictionIO-0.9.5/conf/hadoop/core-site.xml
sed -i "s/<-hdfs-namenode->/master-0.recommendations.$stagetag.internal/g" PredictionIO-0.9.5/conf/hadoop/mapred-site.xml

mkdir PredictionIO-0.9.5/vendors

# Install hadoop
tar zxfC /root/files/packages/spark-1.5.2-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

# Install elasticsearch
tar zxfC /root/files/packages/elasticsearch-1.4.4.tar.gz PredictionIO-0.9.5/vendors
cp /root/files/elasticsearch-conf/elasticsearch.yml PredictionIO-0.9.5/vendors/elasticsearch-1.4.4/config/

# Install hbase
tar zxfC /root/files/packages/hbase-1.0.3-bin.tar.gz PredictionIO-0.9.5/vendors
cp /root/files/hbase-conf/hbase-env.sh PredictionIO-0.9.5/vendors/hbase-1.0.3/conf/
cp /root/files/hbase-conf/hbase-site.xml PredictionIO-0.9.5/vendors/hbase-1.0.3/conf/
sed -i "s/<-hdfs-namenode->/master-0.recommendations.$stagetag.internal/g" PredictionIO-0.9.5/vendors/hbase-1.0.3/conf/hbase-site.xml

mkdir PredictionIO-0.9.5/conf/yarn
cp /root/files/yarn-conf/core-site.xml.template PredictionIO-0.9.5/conf/yarn/
cp /root/files/yarn-conf/yarn-site.xml.template PredictionIO-0.9.5/conf/yarn/

# Install engine
mkdir PredictionIO-0.9.5/engines
aws s3 cp s3://recommendations-dist/PROD/common/engine.tar.gz .
tar zxfC engine.tar.gz PredictionIO-0.9.5/engines

# Install training scripts
cp /root/files/train.sh PredictionIO-0.9.5/bin/
cp /root/files/create-cluster.sh PredictionIO-0.9.5/bin/
cp /root/files/discover-spark-cluster.sh PredictionIO-0.9.5/bin/
cp /root/files/terminate-clusters.sh PredictionIO-0.9.5/bin/
cp /root/files/update-yarn-config.sh PredictionIO-0.9.5/bin/
cp /root/files/discover-elasticsearch-cluster.sh PredictionIO-0.9.5/bin/
cp /root/files/update-pio-config.sh PredictionIO-0.9.5/bin/
cp /root/files/update-engine-config.sh PredictionIO-0.9.5/bin/
cp /root/files/set-asg-size.sh PredictionIO-0.9.5/bin/

# Fix java home
cp /root/files/java_home.sh /etc/profile.d/

chown ubuntu.ubuntu -R PredictionIO-0.9.5

sudo -u ubuntu -s /opt/PredictionIO-0.9.5/bin/train.sh $stagetag > /opt/PredictionIO-0.9.5/train.log 2>&1