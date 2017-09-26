#!/bin/bash

PIDFILE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/node-screen.pid
echo "$PIDFILE"

# ASSUMES THAT THE DIRECTORY THIS FILE IS LOCATED IN HAS THE SAME NAME AS THE PORT OF THIS NODE!!!
PORT=${PWD##*/}

echo "launching node at port ${PORT}"
screen -S "${PORT}" -D -m java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher &
# save process id to be used later
echo $! > "$PIDFILE"

