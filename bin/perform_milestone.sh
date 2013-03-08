#!/bin/sh
set -e

MILESTONE_NUM=$1
TAG_MESSAGE=$2

if [[ ! "${MILESTONE_NUM}" =~ ^[0-9]+$ || "${TAG_MESSAGE}" =~ ^\ *$ ]]
then
  echo "Usage: perform_milestone.sh <number> <tag-message>"
  exit 1
fi

MILESTONE_TAG="0.0.0.M${MILESTONE_NUM}"

git fetch --tags
if git show-ref -q --tags refs/tags/milestones/${MILESTONE_TAG}
then
  echo "Tag milestones/${MILESTONE_TAG} already exists"
  exit 1
fi

if ! git show-ref -q refs/heads/staging-${MILESTONE_TAG}
then
  echo "Branch staging-${MILESTONE_TAG} does not exist"
  exit 1
fi

rm -fr target/checkout

git clone -l --branch staging-${MILESTONE_TAG} . target/checkout

GPG_KEYNAME=${GPG_KEYNAME:-${USER}}

mvn deploy -Psonatype-oss-release -Dgpg.keyname=${GPG_KEYNAME} -f target/checkout/pom.xml \
  -Ddescription="${PWD##*/}/${MILESTONE_TAG} : ${TAG_MESSAGE}"

git tag -u ${GPG_KEYNAME} milestones/${MILESTONE_TAG} staging-${MILESTONE_TAG} -m "${TAG_MESSAGE}"

git tag -v milestones/${MILESTONE_TAG}

git checkout master ; git merge staging-${MILESTONE_TAG}

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=0.0.0-SNAPSHOT

git add . ; git commit -m "Prepare for next round of development"

