#!/bin/sh 

TARGET_FILE=$0

cd `dirname $TARGET_FILE`
TARGET_FILE=`basename $TARGET_FILE`

# Iterate down a (possible) chain of symlinks
while [ -L "$TARGET_FILE" ]
do
    TARGET_FILE=`readlink $TARGET_FILE`
    cd `dirname $TARGET_FILE`
    TARGET_FILE=`basename $TARGET_FILE`
done

PHYS_DIR=`pwd -P`
RESULT=$PHYS_DIR/$TARGET_FILE

BINDIR=$(dirname $RESULT)

java -Xmx512M -DAppDir=${BINDIR}/.. -cp "${BINDIR}/../conf:${BINDIR}/../lib/*" com.insightng.backup.BackupData $*