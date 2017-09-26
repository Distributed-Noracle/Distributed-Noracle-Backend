#!/bin/bash

ENDPOINT="https://steen.informatik.rwth-aachen.de:9082/distributed-noracle/v0.5.0"

SPACE_CREATOR_AGENT_ID="762a164ba8eb06d3d110eeeb0321cb75f2624ab311f9c0c4d0e91bdc854a02f389b91573d6c29d70d3071a8a1738cd20fac87d638777dbfcef4ded8be71e2656"
SPACE_CREATOR_AGENT_LOGIN="noracle-example-smith"

AGENT_PW="testtest"

QUESTION_TEXT="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi."

# create example spaces

for num in {1..5}; do
  curl -s -D - --user noracle-example-smith:${AGENT_PW} -X POST --header 'Content-Type: application/json' -d '{ "name": "example-space-'"${num}"'" }' "${ENDPOINT}"'/spaces' --insecure
done


# get all subscribed for agent smith

curl -s --user noracle-example-smith:${AGENT_PW} -X GET --header 'Accept: application/json' "${ENDPOINT}"'/agents/'"${SPACE_CREATOR_AGENT_ID}"'/spacesubscriptions' --insecure | jq -r '.[].spaceId' | while read spaceId ; do
  for num in {1..50}; do
    curl -s -D - --user noracle-example-smith:${AGENT_PW} -X POST --header 'Content-Type: application/json' -d '{ "questionText": "'"${QUESTION_TEXT}"'" }' "${ENDPOINT}"'/spaces/'"${spaceId}"'/questions' --insecure
  done
done


