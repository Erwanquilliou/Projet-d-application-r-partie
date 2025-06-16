#!/bin/bash
javac -cp "ojdbc17.jar:json-20131018.jar" src/RMI/*.java
cd "$(dirname "$0")/src" || exit 1
echo "lancer ServiceWaze"
java  -cp ".:../mysql-connector-j-9.3.0.jar:../json-20131018.jar" RMI.LancerWazeServices $1
cd - > /dev/null