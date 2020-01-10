#!/usr/bin/env bash

ENDPOINT_DOMAIN_NAME="$K8S_LOAD_BALANCER"
ENVIRONMENT="$K8S_ENVIRONMENT"
PATHS=(/fhir/v0/r4/metadata, /fhir/v0/r4/.well-known/smart-configuration, /fhir/v0/dstu2/metadata, /fhir/v0/dstu2/.well-known/smart-configuration, /fhir/v0/argonaut/data-query/metadata, /fhir/v0/argonaut/data-query/.well-known/smart-configuration)
SUCCESS=0
FAILURE=0

usage() {
  cat <<EOF
  Commands
    smoke-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>]
    regression-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>]

  Example
    smoke-test
      --endpoint-domain-name= localhost
      --environment=qa


$1
EOF
exit 1
}

doCurl () {
  REQUEST_URL="$ENDPOINT_DOMAIN_NAME$path"
  status_code=$(curl -k --write-out %{http_code} --silent --output /dev/null "$REQUEST_URL")

  if [[ "$status_code" == $1 ]]
  then
    SUCCESS=$((SUCCESS + 1))
    echo "$REQUEST_URL: $status_code - Success"
  else
    FAILURE=$((FAILURE + 1))
    echo "$REQUEST_URL: $status_code - Fail"
  fi
}

doSmokeTest () {
  if [[ ! "$ENDPOINT_DOMAIN_NAME" == http* ]]; then
    ENDPOINT_DOMAIN_NAME="https://$ENDPOINT_DOMAIN_NAME"
  fi

  for path in "${PATHS[@]}"
  do
    doCurl 200
  done

  TOTAL=$((SUCCESS + FAILURE))

  echo " TOTAL: $TOTAL | SUCCESS: $SUCCESS | FAILURE: $FAILURE "

  if [[ $FAILURE -gt 0 ]]; then
    exit 1
  fi
}

doRegressionTest () {
  doSmokeTest
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "endpoint-domain-name:,environment:,help" \
    -o "d:e:h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -d|--endpoint-domain-name) ENDPOINT_DOMAIN_NAME=$2;;
    -e|--environment) ENVIRONMENT=$2;;
    -h|--help) usage 'Help:';;
    --) shift;break;;
  esac
  shift;
done

if [[ -z "$ENDPOINT_DOMAIN_NAME" || -e "$ENDPOINT_DOMAIN_NAME" ]]; then
  usage "Missing variable K8S_LOAD_BALANCER or option --endpoint-domain-name|-d."
fi

if [[ -z "$ENVIRONMENT" || -e "$ENVIRONMENT" ]]; then
  usage "Missing variable K8S_ENVIRONMENT or option --environment|-e."
fi

[ $# == 0 ] && usage "No command specified"
COMMAND=$1
shift

case "$COMMAND" in
  s|smoke-test) doSmokeTest;;
  r|regression-test) doRegressionTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
