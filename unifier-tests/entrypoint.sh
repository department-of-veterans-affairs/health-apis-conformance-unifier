#!/usr/bin/env bash

ENDPOINT_DOMAIN_NAME="$K8S_LOAD_BALANCER"
ENVIRONMENT="$K8S_ENVIRONMENT"
TARGETS=(
  "/fhir/v0/r4/metadata                                           application/fhir+json" \
  "/fhir/v0/r4/.well-known/smart-configuration                    application/json" \
  "/fhir/v0/dstu2/metadata                                        application/fhir+json" \
  "/fhir/v0/dstu2/.well-known/smart-configuration                 application/json" \
  "/fhir/v0/argonaut/data-query/metadata                          application/fhir+json" \
  "/fhir/v0/argonaut/data-query/.well-known/smart-configuration   application/json")

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

#
# curl and verify:
# a) expected status code (200)
# b) expected content-type
# c) body contains no newlines
# d) body contains no tabs
#
doCurl () {
  local request_url=$1
  local expected_content_type=$2

  local body=$(mktemp)
  local headers=$(mktemp)

  curl --silent -D $headers --output $body -H "Accept: $expected_content_type" "$request_url"

  local status_code=$(awk '/^HTTP/ {print $2}' $headers)
  local content_type=$(awk '/^Content-Type:/ {print $2}' $headers | tr -dc '[[:print:]]')
  local contains_newlines=$(cat $body | wc -l)
  local contains_tabs=$(grep -P '\t' $body | wc -l)

  if [[ "$status_code" == "200" ]] &&
     [[ "$content_type" == "$expected_content_type"* ]] &&
     [[ $contains_newlines == 0 ]] &&
     [[ $contains_tabs == 0 ]]; then
    SUCCESS=$((SUCCESS + 1))
    echo "$request_url - Success"
  else
    FAILURE=$((FAILURE + 1))
    echo "$request_url - Fail"
  fi

  echo "...http_code: $status_code"
  echo "...content_type: $content_type"
  echo "...contains newlines: $contains_newlines"
  echo "...contains tabs: $contains_tabs"

  rm $body
  rm $headers
}

doSmokeTest () {
  if [[ ! "$ENDPOINT_DOMAIN_NAME" == http* ]]; then
    ENDPOINT_DOMAIN_NAME="https://$ENDPOINT_DOMAIN_NAME"
  fi

  for target in "${TARGETS[@]}"; do
    local args=($target)
    doCurl $ENDPOINT_DOMAIN_NAME${args[0]} ${args[1]}
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
