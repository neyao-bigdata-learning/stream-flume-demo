#!/bin/sh

if test $( pgrep -f flume | wc -l ) -ne 0
then
kill -9 $(ps -ef | grep flume | grep -v grep | awk '{print $2}')
fi

