#!/bin/bash

cd frontend && find "noracle/" -type f -exec curl --user "noracle-example-smith:testtest" --form "filecontent=@{};filename={}" --form identifier="{}" http://localhost:9082/fileservice/files --insecure \; -exec echo "" \;
