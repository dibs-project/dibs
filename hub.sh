#!/bin/sh

CLASSPATH="$(mvn -q -Dmdep.outputFile=/dev/stdout dependency:build-classpath)"
CLASSPATH="target/classes:$CLASSPATH"
export CLASSPATH

java de.huberlin.cms.hub.ui.Ui
