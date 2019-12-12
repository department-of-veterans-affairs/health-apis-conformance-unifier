#!/usr/bin/env bash

set -o pipefail

#
# Blindly trust the specified self signed host.
#
trustServer() {
  local host=$1
  curl -sk https://$host > /dev/null 2>&1
  [ $? == 6 ] && return
  echo "Trusting $host"
  local cacertsDir="$JAVA_HOME/jre/lib/security/cacerts"
  [ -f "$JAVA_HOME/lib/security/cacerts" ] && cacertsDir="$JAVA_HOME/lib/security/cacerts"
  keytool -printcert -rfc -sslserver $host > $host.pem
  keytool \
    -importcert \
    -file $host.pem \
    -alias $host \
    -keystore $cacertsDir \
    -storepass changeit \
    -noprompt
}

# If env var exists then trust the server.
if [ ! -z $K8S_LOAD_BALANCER ]; then
  trustServer $K8S_LOAD_BALANCER
fi

# Run unifier with all args.
java -jar /opt/va/conformance-unifier*.jar $@
