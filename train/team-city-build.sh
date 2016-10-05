#!/bin/bash -e

script="$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"
base="$(dirname "${script}")"
buildStamp="$1"
target="$base/target"

rm -rf "$target"
mkdir -p "$target"

#
# START Build train
#

cp -r "$base/files" "$target/files"
mkdir "$target/files/packages"

cd "$target/files/packages"

wget -nv https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
wget -nv http://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz
wget -nv https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.4.4.tar.gz
wget -nv http://mirrors.ukfast.co.uk/sites/ftp.apache.org/hbase/hbase-1.0.2/hbase-1.0.2-bin.tar.gz

mkdir -p "$target/packages/train"
cd $target
tar czf "$target/packages/train/package.tar.gz" files

rm -rf "$target/files"

#
# END Build train
#

cp "$base/deploy.json" "$target/"

cd "$target"

zip -r artifacts.zip *

echo "##teamcity[publishArtifacts '$target/artifacts.zip => .']"