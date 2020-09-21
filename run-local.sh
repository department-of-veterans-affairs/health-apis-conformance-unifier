#!/usr/bin/env bash

set -e

#=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~

usage() {
  cat <<EOF
Usage:
  $0 <command> <fhir-version>

  Commands:
    start     Build the local app and start a local S3 docker image for testing
    stop      Stop the docker image used for testing
    unify     Run the unifier to unify files (Optional: Use <fhir-version> to specify a specific version to unify)
    help      Display this menu


${1:-}
EOF
}

#=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~

main() {
  case "${1:-}" in
    start) buildLocalApplication && startLocalDockerImage;;
    stop) stopLocalDockerImage;;
    unify) unifySamples "${2:-ALL}";;
    *) usage "Just don't. Don't even. I can't.";;
  esac
  exit 0
}

#=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~

buildLocalApplication() {
  echo "Building Application..."
  mvn -Plocaltest clean install -pl conformance-unifier -DskipTests -Djacoco.skip
}

startLocalDockerImage() {
  echo "Starting local S3 Docker Image..."
  mvn -Plocaltest docker:start -pl conformance-docker-awss3
}

stopLocalDockerImage() {
  echo "Stopping Local S3 Docker Image..."
  mvn -Plocaltest docker:stop -pl conformance-docker-awss3
}

unifyDstuTwo() {
  echo "Unifying Dstu2 Metadata"
  java -Dapp.name=unifier -jar ${UNIFIER_JAR} \
     'dstu2' \
     'metadata' \
     'https://sandbox-api.va.gov/services/fhir/v0/dstu2/metadata'
  echo "Unifying Dstu2 Well-Known"
  java -Dapp.name=unifier -jar ${UNIFIER_JAR} \
     'dstu2' \
     'smart-configuration' \
     'https://sandbox-api.va.gov/services/fhir/v0/dstu2/.well-known/smart-configuration'
}

unifyRFour() {
  echo "Unifying R4 Metadata"
  java -Dapp.name=unifier -jar ${UNIFIER_JAR} \
    'r4' \
    'metadata' \
    'https://sandbox-api.va.gov/services/fhir/v0/r4/metadata'
  echo "Unifying R4 Well-Known"
  java -Dapp.name=unifier -jar ${UNIFIER_JAR} \
    'r4' \
    'smart-configuration' \
    'https://sandbox-api.va.gov/services/fhir/v0/r4/.well-known/smart-configuration'
}

unifySamples() {
  UNIFIER_JAR=$(find -name 'conformance-unifier-*.jar' | head -n +1)
  [ -z "${UNIFIER_JAR:-}" ] && echo "Rebuild Application and Try Again." && exit 1
  case "${1:-}" in
    dstu2) unifyDstuTwo;;
    r4) unifyRFour;;
    *) unifyDstuTwo && unifyRFour;;
  esac
    
  cat <<EOF
Unified documents placed in local S3 bucket. To view, use the following S3 commands:

   # List bucket name:
   aws s3api list-buckets --query "Buckets[].Name" --endpoint-url http://localhost:9090

   # List objects in bucket:
   aws s3api list-objects --bucket testbucket --endpoint-url http://localhost:9090 --query 'Contents[].{Key: Key, Size: Size}'

   # Get most recent object replacing dashes:
   aws s3api --endpoint-url http://localhost:9090 list-objects-v2 --bucket "testbucket" --query 'reverse(sort_by(Contents, &LastModified))[:1].Key' --output=text | awk '{gsub(/%2D/,"-")}1'

   # Copy the specified object to stdout (example showing r4-capability):
   aws s3 --endpoint-url http://localhost:9090 cp s3://testbucket/r4-capability -

   # See the metadata for the specified object to stdout (example showing dstu-smart-configuration):
   aws s3api get-object --bucket testbucket --endpoint-url http://localhost:9090 --key dstu2-metadata /dev/null
EOF
}

#=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~

main $@
