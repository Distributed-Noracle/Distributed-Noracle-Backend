#!/bin/bash

PIDFILE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/node-screen.pid
echo "$PIDFILE"

# ASSUMES THAT THE DIRECTORY THIS FILE IS LOCATED IN HAS THE SAME NAME AS THE PORT OF THIS NODE!!!
PORT=${PWD##*/}

echo "stopping node at port ${PORT}"
while read pid; do
  echo "killing process $pid"
  kill $pid
done < "$PIDFILE"
/bin/rm "$PIDFILE"

