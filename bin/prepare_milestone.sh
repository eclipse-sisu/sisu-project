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

MILESTONE_NUM=$1

if [[ ! "${MILESTONE_NUM}" =~ ^[0-9]+$ ]]
then
  echo "Usage: prepare_milestone.sh <number>"
  exit 1
fi

MILESTONE_TAG="0.0.0.M${MILESTONE_NUM}"

git fetch --tags
if git show-ref -q --tags refs/tags/milestones/${MILESTONE_TAG}
then
  echo "Tag milestones/${MILESTONE_TAG} already exists"
  exit 1
fi

if git show-ref -q refs/heads/staging-${MILESTONE_TAG}
then
  echo "Branch staging-${MILESTONE_TAG} already exists"
  exit 1
fi

git checkout --no-track -b staging-${MILESTONE_TAG} master

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${MILESTONE_TAG}

git add . ; git commit -m "Milestone ${MILESTONE_TAG}"

MILESTONE_OLD="0.0.0.M$((MILESTONE_NUM-1))"

if git show-ref -q --tags refs/tags/milestones/${MILESTONE_OLD}
then
  git diff milestones/${MILESTONE_OLD} -w --patience --color-words=[^[:space:]]
fi

