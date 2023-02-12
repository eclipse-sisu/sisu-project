#*******************************************************************************
# Copyright (c) 2022-present Sonatype, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Stuart McCulloch - initial API and implementation
#*******************************************************************************
#!/bin/sh
set -e

TAG=$1

if [[ ! "${TAG}" =~ ^ASM_[0-9A-Z_]+$ ]]
then
  echo "Usage: update_asm.sh <tag>"
  exit 1
fi

SRC=asm-${TAG}/asm/src/main/java/org/objectweb/asm
DST=org.eclipse.sisu.inject/src/main/java/org/eclipse/sisu/space/asm/

rm ${DST}/*.java
curl -s https://gitlab.ow2.org/asm/asm/-/archive/${TAG}/asm-${TAG}.zip | jar x ${SRC}

for f in ${SRC}/*.java
do
  sed -e "s@org/objectweb/asm@org/eclipse/sisu/space/asm@" \
    -e "s@org\.objectweb\.asm@org.eclipse.sisu.space.asm@" \
  $f > ${DST}/`basename $f`
done

rm -r ${SRC}/*
rmdir -p ${SRC}

