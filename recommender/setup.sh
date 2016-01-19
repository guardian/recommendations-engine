#!/bin/bash

cd /opt/

wget https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
tar zxvf PredictionIO-0.9.5.tar.gz

mkdir /opt/PredictionIO-0.9.5/plugins
aws s3 cp s3://prediction-io-dist/mobile-prediction-io/PROD/prediction-io-plugins/predictionio-storage-s3-assembly-0.1.jar /opt/PredictionIO-0.9.5/plugins/

mkdir /opt/PredictionIO-0.9.5/engines
tar zxvf /root/engine.tar.gz --directory /opt/PredictionIO-0.9.5/engines/

mkdir PredictionIO-0.9.5/vendors

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.5.1-bin-hadoop2.6.tgz
tar zxvfC spark-1.5.1-bin-hadoop2.6.tgz PredictionIO-0.9.5/vendors

wget http://mirrors.ukfast.co.uk/sites/ftp.apache.org/hbase/hbase-1.0.2/hbase-1.0.2-bin.tar.gz
tar zxvfC hbase-1.0.2-bin.tar.gz PredictionIO-0.9.5/vendors

cp /root/files/hbase-env.sh PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
cp /root/files/hbase-site.xml PredictionIO-0.9.5/vendors/hbase-1.0.2/conf/
cp /root/files/pio-env.sh PredictionIO-0.9.5/conf/

mkdir PredictionIO-0.9.5/conf/hadoop
cp /root/files/core-site.xml PredictionIO-0.9.5/conf/hadoop/
cp /root/files/mapred-site.xml PredictionIO-0.9.5/conf/hadoop/

cp /root/files/java_home.sh /etc/profile.d/

cp /root/files/recommender.conf /etc/init/

chown ubuntu.ubuntu -R PredictionIO-0.9.5

service recommender start