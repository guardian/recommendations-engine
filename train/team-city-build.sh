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