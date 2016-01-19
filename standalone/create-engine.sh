#!/bin/bash

cd /opt/PredictionIO-0.9.5/

mkdir engines

cd engines

yes | pio template get PredictionIO/template-scala-parallel-universal-recommendation \
    --version v0.2.3 \
    --name 'TheGuardianNews&Media' \
    --package 'com.gu' \
    --email 'mobile.server.side@theguardian.com' \
    MyGuardianTestEngine-1

pio app new MyGuardianTestEngine-1

cp /root/files/engine.json MyGuardianTestEngine-1/

pio build
# pio train
# pio train -- --master spark://<spark-master>:7077 --driver-memory 1200M
# pio deploy