#!/bin/bash

rm -rf target

mkdir target
mkdir target/pio-dist

cp pio-dist/* target/pio-dist/

cd target
tar -czf pio-dist.tar.gz pio-dist
aws s3 cp pio-dist.tar.gz s3://prediction-io-dist/mobile-prediction-io/PROD/recommender/pio-dist.tar.gz
aws s3 cp ../engine.tar.gz s3://prediction-io-dist/mobile-prediction-io/PROD/recommender/engine.tar.gz
