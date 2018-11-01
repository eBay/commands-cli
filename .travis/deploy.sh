#!/usr/bin/env bash

set -e
cd "$(dirname $0)/.."

function __deploy {
  .travis/mvn deploy -s .travis/bintray-settings.xml -DskipTests
}

function __get_project_version {
  .travis/mvn help:evaluate -Dexpression=project.version -Doutput=version.txt > /dev/null
  local version="$(cat version.txt)"
  rm version.txt
  echo "${version}"
}

function __sign {
  local version=$1
  .travis/bintray sign ${version}
}

function __publish {
  local version=$1
  .travis/bintray publish ${version}
}

function __sync {
  local version=$1
  if [[ "$SONATYPE_OSS_USER" == "" ]]; then
    echo "Sonatype OSS user is not defined, skipping sync to maven central"
  else
    .travis/bintray sync ${version}
  fi
}

###############################################

version="$(__get_project_version)"
echo "Project version: ${version}"

__deploy
__sign ${version}
__publish ${version}
__sync ${version}