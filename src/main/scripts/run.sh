#!/bin/sh
DIR=$(cd `dirname $0`; pwd)
cd ${DIR}
rm -rf flume.log
rm -f logs/*

nohup bin/flume-ng agent --conf conf --conf-file conf/flume-conf.properties --name agent -Dflume.monitoring.type=http -Dflume.monitoring.port=34545 >> flume.log 2>&1 &
