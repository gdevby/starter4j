#/bin/bash
mvn -N clean deploy -P remote_deploy
cd desktop-common-util
mvn clean deploy -P remote_deploy