#!/usr/bin/env bash

set -o pipefail

java -jar /opt/va/conformance-unifier*.jar $@
