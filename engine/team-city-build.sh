#!/bin/bash -e

script="$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"
base="$(dirname "${script}")"
buildStamp="$1"
target="$base/target"

rm -rf "$target"
mkdir -p "$target"

#
# START Build engine
#

mkdir "$target/build"
cd "$target/build"

wget -nv https://d8k1yxp8elc6b.cloudfront.net/PredictionIO-0.9.5.tar.gz
tar zxf PredictionIO-0.9.5.tar.gz

mkdir PredictionIO-0.9.5/engines
cd PredictionIO-0.9.5/engines

yes | ../bin/pio template get PredictionIO/template-scala-parallel-universal-recommendation \
    --version v0.2.3 \
    --name 'TheGuardianNews&Media' \
    --package 'com.gu' \
    --email 'mobile.server.side@theguardian.com' \
    MyGuardianTestEngine-1

cd MyGuardianTestEngine-1
cp "$base/engine.json" .
cp "$base/manifest.json" . # this should be generated rather than being committed to the repo
cp "$base/pio.sbt" .
../../sbt/sbt package assemblyPackageDependency

mkdir -p "$target/packages/common"
cd ..
tar czf "$target/packages/common/engine.tar.gz" MyGuardianTestEngine-1

cd "$target/"
rm -rf build

#
# END Build engine
#

cp "$base/deploy.json" "$target/"

cd "$target"

zip -r artifacts.zip *

echo "##teamcity[publishArtifacts '$target/artifacts.zip => .']"