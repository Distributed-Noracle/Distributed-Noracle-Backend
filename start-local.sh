#!/bin/bash

PORT=9012

echo "launching node at port ${PORT}"
java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher
