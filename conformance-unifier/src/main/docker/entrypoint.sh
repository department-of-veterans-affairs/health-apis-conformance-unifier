#!/usr/bin/env bash

set -o pipefail

java -jar /test-suite/conformance-unifier*.jar $@
