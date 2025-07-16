#!/bin/sh
set -e

CURRENT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -nsu | grep -e '^[[:digit:]]')

if [ "${CURRENT_VERSION}" = "" ]; then
  echo "Could not find current version from Maven"
  exit 1
fi

if [ -z $(echo "${CURRENT_VERSION}" | grep SNAPSHOT) ]; then
  echo "This job only deploys SNAPSHOT versions, skipping version ${CURRENT_VERSION}"
  exit 0
fi

echo "Building version ${CURRENT_VERSION}"
mvn -s ./.mvn/settings.xml -B clean deploy -DskipTests -Dsonatype.auto.publish=true -Pcoherence-ce -Dgithub.build=true
