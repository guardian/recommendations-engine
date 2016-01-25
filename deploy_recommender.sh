#!/bin/bash

rm -rf target
mkdir target
mkdir target/files
cp recommender/files/* target/files/
cd target
tar -czf package.tar.gz files
aws s3 cp package.tar.gz s3://prediction-io-dist/mobile-prediction-io/PROD/recommender/package.tar.gz
aws s3 cp ../engine.tar.gz s3://prediction-io-dist/mobile-prediction-io/PROD/recommender/engine.tar.gz
cd ../
