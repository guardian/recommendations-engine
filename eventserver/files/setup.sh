#!/bin/bash

instanceid=`curl -s http://169.254.169.254/latest/meta-data/instance-id`
region=`curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's/.$//'`

stagetag=`aws ec2 describe-tags --filters "Name=resource-id,Values=$instanceid" "Name=resource-type,Values=instance" "Name=key,Values=Stage" --region $region | grep -oP "(?<=\"Value\": \")[^\"]+"`

HADOOP_MASTER=namenode.hadoop.recommendations.$stagetag.internal.
ELASTICSEARCH_HOSTS=nodes.elasticsearch.recommendations.$stagetag.internal.

cd /opt/

wget https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
tar zxf PredictionIO-0.9.5.tar.gz

mkdir /opt/PredictionIO-0.9.5/engines
tar zxf /root/engine.tar.gz --directory /opt/PredictionIO-0.9.5/engines/

mkdir PredictionIO-0.9.5/vendors

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz
tar zxfC spark-1.5.2-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

wget http://mirrors.ukfast.co.uk/sites/ftp.apache.org/hbase/hbase-1.0.2/hbase-1.0.2-bin.tar.gz
tar zxfC hbase-1.0.2-bin.tar.gz PredictionIO-0.9.5/vendors

cp /root/files/hbase-env.sh PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
sed 's/<HADOOP-MASTER>/'$HADOOP_MASTER'/g' /root/files/hbase-site.xml.template > PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/hbase-site.xml

sed 's/<ELASTICSEARCH-HOSTS>/'$ELASTICSEARCH_HOSTS'/g' /root/files/pio-env.sh.template > PredictionIO-0.9.5/conf/pio-env.sh

mkdir PredictionIO-0.9.5/conf/hadoop
sed 's/<HADOOP-MASTER>/'$HADOOP_MASTER'/g' /root/files/core-site.xml.template > PredictionIO-0.9.5/conf/hadoop/core-site.xml
sed 's/<HADOOP-MASTER>/'$HADOOP_MASTER'/g' /root/files/mapred-site.xml.template > PredictionIO-0.9.5/conf/hadoop/mapred-site.xml

cp /root/files/java_home.sh /etc/profile.d/

cp /root/files/recommendations-eventserver.conf /etc/init/

chown ubuntu.ubuntu -R PredictionIO-0.9.5

service recommendations-eventserver start