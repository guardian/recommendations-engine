#!/bin/bash

instanceid=`curl -s http://169.254.169.254/latest/meta-data/instance-id`
region=`curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's/.$//'`
stagetag=`aws ec2 describe-tags --filters "Name=resource-id,Values=$instanceid" "Name=resource-type,Values=instance" "Name=key,Values=Stage" --region $region | grep -oP "(?<=\"Value\": \")[^\"]+"`

cd /opt/

# Install prediction.io
tar zxf /root/files/packages/PredictionIO-0.9.5.tar.gz

cp /root/files/pio-env.sh PredictionIO-0.9.5/conf/

mkdir PredictionIO-0.9.5/conf/hadoop
cp /root/files/hadoop-conf/core-site.xml PredictionIO-0.9.5/conf/hadoop/
cp /root/files/hadoop-conf/mapred-site.xml PredictionIO-0.9.5/conf/hadoop/

mkdir PredictionIO-0.9.5/vendors

# Install hadoop
tar zxfC /root/files/packages/spark-1.5.2-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

# Install elasticsearch
tar zxfC /root/files/packages/elasticsearch-1.4.4.tar.gz PredictionIO-0.9.5/vendors
cp /root/files/elasticsearch-conf/elasticsearch.yml PredictionIO-0.9.5/vendors/elasticsearch-1.4.4/config/

# Install hbase
tar zxfC /root/files/packages/hbase-1.0.2-bin.tar.gz PredictionIO-0.9.5/vendors
cp /root/files/hbase-conf/hbase-env.sh PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
cp /root/files/hbase-conf/hbase-site.xml PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/

mkdir PredictionIO-0.9.5/conf/yarn
cp /root/files/yarn-conf/core-site.xml.template PredictionIO-0.9.5/conf/yarn/
cp /root/files/yarn-conf/yarn-site.xml.template PredictionIO-0.9.5/conf/yarn/

# Install engine
mkdir PredictionIO-0.9.5/engines
aws s3 cp s3://recommendations-dist/PROD/common/engine.tar.gz .
tar zxfC engine.tar.gz PredictionIO-0.9.5/engines

# Install training scripts
cp /root/files/train.sh PredictionIO-0.9.5/bin/
cp /root/files/discover-spark-cluster.sh PredictionIO-0.9.5/bin/
cp /root/files/update-yarn-config.sh PredictionIO-0.9.5/bin/

# Fix java home
cp /root/files/java_home.sh /etc/profile.d/

chown ubuntu.ubuntu -R PredictionIO-0.9.5

sed "s/\${stage}/$stagetag/" /root/files/cron-conf/train.template > /etc/cron.d/train