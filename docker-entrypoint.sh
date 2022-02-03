#!/usr/bin/env bash

set -e

# print all comands to console if DEBUG is set
if [[ ! -z "${DEBUG}" ]]; then
    set -x
fi

# set some helpful variables
#export SERVICE_PROPERTY_FILE='etc/i5.las2peer.services.servicePackage.akgService.properties'
export WEB_CONNECTOR_PROPERTY_FILE='etc/i5.las2peer.connectors.webConnector.WebConnector.properties'
export SERVICE_VERSION=$(awk -F "=" '/service.version/ {print $2}' gradle.properties)
export SERVICE_NAME=$(awk -F "=" '/service.name/ {print $2}' gradle.properties)

export SERVICE1=${SERVICE_NAME}.'NoracleVoteService'@${SERVICE_VERSION}
export SERVICE2=${SERVICE_NAME}.'NoracleService'@${SERVICE_VERSION}
export SERVICE3=${SERVICE_NAME}.'NoracleSpaceService'@${SERVICE_VERSION}
export SERVICE4=${SERVICE_NAME}.'NoracleQuestionService'@${SERVICE_VERSION}
export SERVICE5=${SERVICE_NAME}.'NoracleQuestionRelationService'@${SERVICE_VERSION}
export SERVICE6=${SERVICE_NAME}.'NoracleAgentService'@${SERVICE_VERSION}
export SERVICE7=${SERVICE_NAME}.'NoracleRecommenderService'@${SERVICE_VERSION}

# Currently, these services cannot be used, because object serialization in the network does not work
export SERVICE8=${SERVICE_NAME}.'NoracleNormalizationService'@${SERVICE_VERSION}
export SERVICE9=${SERVICE_NAME}.'NoracleQuestionUtilityService'@${SERVICE_VERSION}

echo deploy $SERVICE1
echo deploy $SERVICE2
echo deploy $SERVICE3
echo deploy $SERVICE4
echo deploy $SERVICE5
echo deploy $SERVICE6
echo deploy $SERVICE7

function set_in_service_config {
    sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${SERVICE_PROPERTY_FILE}
}
#cp $SERVICE_PROPERTY_FILE $SERVICE_PROPERTY_FILE


# set defaults for optional service parameters
echo set defaults for optional service parameters
[[ -z "${SERVICE_PASSPHRASE}" ]] && export SERVICE_PASSPHRASE='ahs'

# wait for any bootstrap host to be available
echo wait for any bootstrap host to be available
if [[ ! -z "${BOOTSTRAP}" ]]; then
    echo "Waiting for any bootstrap host to become available..."
    for host_port in ${BOOTSTRAP//,/ }; do
        arr_host_port=(${host_port//:/ })
        host=${arr_host_port[0]}
        port=${arr_host_port[1]}
        if { </dev/tcp/${host}/${port}; } 2>/dev/null; then
            echo "${host_port} is available. Continuing..."
            break
        fi
    done
fi

# prevent glob expansion in lib/*
echo prevent glob expansion in lib
set -f
LAUNCH_COMMAND='java -cp lib/* --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED i5.las2peer.tools.L2pNodeLauncher -s service -p '"${LAS2PEER_PORT} ${SERVICE_EXTRA_ARGS}"
if [[ ! -z "${BOOTSTRAP}" ]]; then
    LAUNCH_COMMAND="${LAUNCH_COMMAND} -b ${BOOTSTRAP}"
fi

# it's realistic for different nodes to use different accounts (i.e., to have
# different node operators). this function echos the N-th mnemonic if the
# hostname is "something-something-N". if not, first mnemonic is used
echo define function selectMn
function selectMnemonic {
    PEER_NUM=$(hostname | cut -d'-' -f3) # get N out of las2peer-peer-N
    declare -a mnemonics=("differ employ cook sport clinic wedding melody column pave stuff oak price" "memory wrist half aunt shrug elbow upper anxiety maximum valve finish stay" "alert sword real code safe divorce firm detect donate cupboard forward other" "pair stem change april else stage resource accident will divert voyage lawn" "lamp elbow happy never cake very weird mix episode either chimney episode" "cool pioneer toe kiwi decline receive stamp write boy border check retire" "obvious lady prize shrimp taste position abstract promote market wink silver proof" "tired office manage bird scheme gorilla siren food abandon mansion field caution" "resemble cattle regret priority hen six century hungry rice grape patch family" "access crazy can job volume utility dial position shaft stadium soccer seven")
    if [[ $PEER_NUM =~ ^[0-9]+$ && $PEER_NUM -lt ${#mnemonics[@]} ]]; then
        echo "${mnemonics[$PEER_NUM]}"
    else
        # note: zsh and others use 1-based indexing. this requires bash
        echo "${mnemonics[0]}"
    fi
}

#prepare pastry properties
echo curl s ipinfo ${LAS2PEER_PORT}
echo external_address = $(curl -s https://ipinfo.io/ip):${LAS2PEER_PORT} > etc/pastry.properties

# start the service within a las2peer node
echo node id seed ${NODE_ID_SEED}
echo start the service within a las2peer node
if [[ -z "${@}" ]]
then
    if [ -n "$LAS2PEER_ETH_HOST" ]; then
        echo ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED --ethereum-mnemonic "$(selectMnemonic)" uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE2}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE3}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE4}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE5}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE6}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE7}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector "node=getNodeAsEthereumNode()" "registry=node.getRegistryClient()" "n=getNodeAsEthereumNode()" "r=n.getRegistryClient()"
        exec ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED --ethereum-mnemonic "$(selectMnemonic)" uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE2}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE3}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE4}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE5}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE6}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE7}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector "node=getNodeAsEthereumNode()" "registry=node.getRegistryClient()" "n=getNodeAsEthereumNode()" "r=n.getRegistryClient()"
        #exec ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED --observer --ethereum-mnemonic "$(selectMnemonic)" uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE2}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE3}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE4}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE5}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE6}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector "node=getNodeAsEthereumNode()" "registry=node.getRegistryClient()" "n=getNodeAsEthereumNode()" "r=n.getRegistryClient()"
    else
        echo ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE2}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE3}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE4}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE5}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE6}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE7}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector
        exec ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE2}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE3}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE4}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE5}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE6}""'", "'""${SERVICE_PASSPHRASE}""'"\) startService\("'""${SERVICE7}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector
        #exec ${LAUNCH_COMMAND} --node-id-seed $NODE_ID_SEED --observer uploadStartupDirectory startService\("'""${SERVICE1}""'", "'""${SERVICE_PASSPHRASE}""'"\) startWebConnector
    fi
else
  exec ${LAUNCH_COMMAND} ${@}
fi

echo docker entrypoint finished