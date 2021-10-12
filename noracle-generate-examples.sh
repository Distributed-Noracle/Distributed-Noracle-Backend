#!/bin/bash

endpoint="https://steen.informatik.rwth-aachen.de:9082/distributed-noracle/v1.0.0"

space_creator_agent_id="762a164ba8eb06d3d110eeeb0321cb75f2624ab311f9c0c4d0e91bdc854a02f389b91573d6c29d70d3071a8a1738cd20fac87d638777dbfcef4ded8be71e2656"
space_creator_agent_login="noracle-example-smith"

agent_pw="testtest"

num_spaces=5
num_questions_per_space=25
num_relations_per_space=15

relation_types=()
relation_types+=("\"name\": \"Similarity\", \"directed\": \"false\"")
relation_types+=("\"name\": \"FollowUp\", \"directed\": \"true\"")
relation_types+=("\"name\": \"LinksWith\", \"directed\": \"false\"")

random_lengthLorem() {
  #1703 chars
  question_text="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi."
  # should be possible to at least print 200 chars
  start=$((RANDOM % ((${#question_text} - (RANDOM % 1500)))))
  # max 500 chars, not more then avaible after starting position
  printf '%s' "${question_text:$start:(RANDOM % (500 % (1703-$start)))}"
}

# create example spaces

for (( num=1; num<=${num_spaces}; num++ )); do
  curl -s -D - --user noracle-example-smith:${agent_pw} -X POST --header 'Content-Type: application/json' -d '{ "name": "example-space-'"${num}"'" }' "${endpoint}"'/spaces' --insecure &
done
# wait till all requests terminated
wait


# get all subscribed spaces for agent smith
example_space_ids=()
out="$( { curl -s --user noracle-example-smith:${agent_pw} -X GET --header 'Accept: application/json' "${endpoint}"'/agents/'"${space_creator_agent_id}"'/spacesubscriptions' --insecure ; } 2>&1 )"
while read spaceId ; do
  example_space_ids+=("${spaceId}")
done < <(echo "${out}" | jq -r '.[].spaceId')

for spaceId in "${example_space_ids[@]}" ; do
  # create questions inside space
  question_ids=()
  for (( num=1; num<=${num_questions_per_space}; num++ )); do
    random=$(random_lengthLorem)
    out="$( { curl -s -D - --user noracle-example-smith:"${agent_pw}" -X POST --header 'Content-Type: application/json' -d '{ "text": "'"${random}"'" }' "${endpoint}"'/spaces/'"${spaceId}"'/questions' --insecure ; } 2>&1 )"
    echo "${out}"
    location_header="$( echo "${out}" | tr -d '\r' | sed -En 's/^Location: (.*)/\1/p' )"
    question_id=${location_header##*/}
    if [ -n "${question_ids}" ]; then # relate to existing question (if any exists)
      relation_type=${relation_types[$RANDOM % ${#relation_types[@]} ]}
      first_question_id=${question_ids[$RANDOM % ${#question_ids[@]} ]}
      second_question_id=${question_ids[$RANDOM % ${#question_ids[@]} ]}
      out="$( { curl -s -D - --user noracle-example-smith:"${agent_pw}" -X POST --header 'Content-Type: application/json' -d '{ '"${relation_type}"', "firstQuestionId": "'"${first_question_id}"'", "secondQuestionId": "'"${second_question_id}"'" }' "${endpoint}"'/spaces/'"${spaceId}"'/relations' --insecure ; } 2>&1 )"
      echo "${out}"
    fi
    question_ids+=(${question_id})
  done
  # create random relations between questions
  for (( num=1; num<=${num_relations_per_space}; num++ )); do
    relation_type=${relation_types[$RANDOM % ${#relation_types[@]} ]}
    first_question_id=${question_ids[$RANDOM % ${#question_ids[@]} ]}
    second_question_id=${question_ids[$RANDOM % ${#question_ids[@]} ]}
    out="$( { curl -s -D - --user noracle-example-smith:"${agent_pw}" -X POST --header 'Content-Type: application/json' -d '{ '"${relation_type}"', "firstQuestionId": "'"${first_question_id}"'", "secondQuestionId": "'"${second_question_id}"'" }' "${endpoint}"'/spaces/'"${spaceId}"'/relations' --insecure ; } 2>&1 )"
    echo "${out}"
  done
done

# generate join links
space_join_links=""
for spaceId in "${example_space_ids[@]}" ; do
  out="$( { curl -s -D - --user noracle-example-smith:${agent_pw} -X GET --header 'Accept: application/json' "${endpoint}"'/spaces/'"${spaceId}" --insecure ; } 2>&1 )"
  while read spaceSecret ; do
    space_join_links="${space_join_links}<div><a href=\"http://dbis.rwth-aachen.de/noracle/spaces/${spaceId}?pw=${spaceSecret}\">Click here to join example space ${spaceId}</a></div>"
  done < <(echo "${out}" | jq -r '.spaceSecret')
done

# write invitation links to file
read -d '' join_space_file_content <<- EOF
<html>
<head>
<title>Join A Noracle Example Space</title>
</head>
<body>
<p>Please click on a link below to join the appropriate Noracle example space</p>
${space_join_links}
</body>
</html>

EOF

echo "${join_space_file_content}" > "join-example-space.html"

