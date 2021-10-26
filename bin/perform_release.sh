#*******************************************************************************
# Copyright (c) 2010-present Sonatype, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
#*******************************************************************************
#!/bin/sh
set -e

VERSION=$1
MESSAGE=$2

if [[ ! "${VERSION}" =~ ^0.([0-9]+).([0-9]+)$ || "${MESSAGE}" =~ ^\ *$ ]]
then
  echo "Usage: perform_release.sh <0.?.?> <message>"
  exit 1
fi

git fetch --tags
if git show-ref -q --tags refs/tags/releases/${VERSION}
then
  echo "Tag releases/${VERSION} already exists"
  exit 1
fi

if ! git show-ref -q refs/heads/staging-${VERSION}
then
  echo "Branch staging-${VERSION} does not exist"
  exit 1
fi

rm -fr target/checkout

git clone -l --branch staging-${VERSION} . target/checkout

GPG_KEYNAME=${GPG_KEYNAME:-${USER}}

mvn deploy -Psonatype-oss-release -Dgpg.keyname=${GPG_KEYNAME} -f target/checkout/pom.xml \
  -Ddescription="${PWD##*/}/${VERSION} : ${MESSAGE}"

git tag -u ${GPG_KEYNAME} releases/${VERSION} staging-${VERSION} -m "${MESSAGE}"

git tag -v releases/${VERSION}

git checkout master ; git merge staging-${VERSION}

MINOR_MICRO=${VERSION#*.}
NEW_VERSION=${VERSION%%.*}.$((${MINOR_MICRO%%.*}+1)).0-SNAPSHOT

mvn org.eclipse.tycho:tycho-versions-plugin:2.5.0:set-version -DnewVersion=${NEW_VERSION}

git add . ; git commit -m "Prepare for next round of development"

