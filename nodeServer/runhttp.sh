#!/bin/bash

SCRIPTPATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $SCRIPTPATH/../apienv.sh
node $SCRIPTPATH/server/server.js
