#!/bin/bash
cd "$(dirname "$0")/src" || exit 1
echo "lancer ServiceWaze"
java  -cp ".;../mysql-connector-j-9.3.0.jar;../json-20131018.jar" RMI.LancerWazeServices
cd - > /dev/null