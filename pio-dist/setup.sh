#!/bin/bash

instanceid=`curl -s http://169.254.169.254/latest/meta-data/instance-id`
region=`curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's/.$//'`

stagetag=`aws ec2 describe-tags --filters "Name=resource-id,Values=$instanceid" "Name=resource-type,Values=instance" "Name=key,Values=Stage" --region $region | grep -oP "(?<=\"Value\": \")[^\"]+"`

HADOOP_MASTER=namenode.hadoop.recommendations.$stagetag.internal.
ELASTICSEARCH_HOSTS=nodes.elasticsearch.recommendations.$stagetag.internal.
PIO_DIST=/root/pio-dist

cd /opt/

wget https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
tar zxf PredictionIO-0.9.5.tar.gz

mkdir PredictionIO-0.9.5/vendors

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz
tar zxfC spark-1.5.2-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

mkdir /opt/PredictionIO-0.9.5/engines
tar zxf /root/engine.tar.gz --directory /opt/PredictionIO-0.9.5/engines/

mkdir -p PredictionIO-0.9.5/conf/hbase/

cp ${PIO_DIST}/hbase-env.sh PredictionIO-0.9.5/conf/hbase/
sed 's/<HADOOP-MASTER>/'$HADOOP_MASTER'/g' ${PIO_DIST}/hbase-site.xml.template > PredictionIO-0.9.5/conf/hbase/hbase-site.xml

sed 's/<ELASTICSEARCH-HOSTS>/'${ELASTICSEARCH_HOSTS}'/g' ${PIO_DIST}/pio-env.sh.template > PredictionIO-0.9.5/conf/pio-env.sh

cp ${PIO_DIST}/java_home.sh /etc/profile.d/
cp ${PIO_DIST}/pio-eventserver.conf /etc/init/
cp ${PIO_DIST}/pio-deploy.conf /etc/init/

chown ubuntu.ubuntu -R PredictionIO-0.9.5
