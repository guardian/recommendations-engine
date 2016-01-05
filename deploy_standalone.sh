#!/bin/bash

rm -rf target
mkdir target
mkdir target/files
cp standalone/* target/files/
cd target
tar -czf package.tar.gz files
aws s3 cp package.tar.gz s3://prediction-io-dist/mobile-prediction-io/PROD/standalone/package.tar.gz
cd ../