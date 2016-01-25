#!/bin/bash

cd /opt/

wget https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
tar zxf PredictionIO-0.9.5.tar.gz

mkdir PredictionIO-0.9.5/vendors

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz
tar zxfC spark-1.5.2-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.4.4.tar.gz
tar zxfC elasticsearch-1.4.4.tar.gz PredictionIO-0.9.5/vendors

wget http://mirrors.ukfast.co.uk/sites/ftp.apache.org/hbase/hbase-1.0.2/hbase-1.0.2-bin.tar.gz
tar zxfC hbase-1.0.2-bin.tar.gz PredictionIO-0.9.5/vendors

cp /root/files/elasticsearch.yml PredictionIO-0.9.5/vendors/elasticsearch-1.4.4/config/
cp /root/files/hbase-env.sh PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
cp /root/files/hbase-site.xml PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
cp /root/files/pio-env.sh PredictionIO-0.9.5/conf/

mkdir PredictionIO-0.9.5/conf/hadoop
cp /root/files/core-site.xml PredictionIO-0.9.5/conf/hadoop/
cp /root/files/mapred-site.xml PredictionIO-0.9.5/conf/hadoop/

cp /root/files/java_home.sh /etc/profile.d/

chown ubuntu.ubuntu -R PredictionIO-0.9.5

cp /root/files/get-recommendations /home/ubuntu/
chown ubuntu.ubuntu /home/ubuntu/get-recommendations

sudo su ubuntu -c /opt/PredictionIO-0.9.5/bin/pio-start-all