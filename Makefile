
build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
deploy-ossrh:
	clean deploy -Possrh
