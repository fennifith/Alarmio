#!/bin/bash

eval "cat << EOF
$(<bintray-format.json)
EOF
" > bintray.json