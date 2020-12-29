#!/bin/bash
mvn clean compile assembly:single
cd target
jar uvf IntelligentAgentsProject-1.0-SNAPSHOT-jar-with-dependencies.jar -C ../src/main/java/ .
cp IntelligentAgentsProject-1.0-SNAPSHOT-jar-with-dependencies.jar group31.jar
