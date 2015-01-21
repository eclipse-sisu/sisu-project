#*******************************************************************************
# Copyright (c) 2010, 2013 Sonatype, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
#*******************************************************************************
#!/bin/sh
set -e

VERSION=$1

if [[ ! "${VERSION}" =~ ^0.([0-9]+).([0-9]+).M([0-9]+)$ ]]
then
  echo "Usage: prepare_milestone.sh <0.?.?.M?>"
  exit 1
fi

git fetch --tags
if git show-ref -q --tags refs/tags/milestones/${VERSION}
then
  echo "Tag milestones/${VERSION} already exists"
  exit 1
fi

if git show-ref -q refs/heads/staging-${VERSION}
then
  echo "Branch staging-${VERSION} already exists"
  exit 1
fi

git checkout --no-track -b staging-${VERSION} master

mvn org.eclipse.tycho:tycho-versions-plugin:0.22.0:set-version -DnewVersion=${VERSION}

git add . ; git commit -m "Milestone ${VERSION}"

