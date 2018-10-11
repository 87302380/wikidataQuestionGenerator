#!/usr/bin/env bash
cd $(dirname $0)
mkdir -p src/main/resources
cd src/main/resources
wget http://jendrik.eu/wwm/firstlines2.json
wget http://jendrik.eu/wwm/firstlines.json.gz
wget http://jendrik.eu/wwm/washington.json
