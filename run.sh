#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ "x$CASSANDRA_HOME" != "x" ]; then
    CASSANDRA_INCLUDE=$CASSANDRA_HOME/bin/cassandra.in.sh
fi

if [ "x$CASSANDRA_INCLUDE" = "x" ]; then
    for include in "`dirname "$0"`/cassandra.in.sh" \
                   "$HOME/.cassandra.in.sh" \
                   /usr/share/cassandra/cassandra.in.sh \
                   /usr/local/share/cassandra/cassandra.in.sh \
                   /opt/cassandra/cassandra.in.sh; do
        if [ -r "$include" ]; then
            . "$include"
            break
        fi
    done
elif [ -r "$CASSANDRA_INCLUDE" ]; then
    . "$CASSANDRA_INCLUDE"
fi


# Use JAVA_HOME if set, otherwise look for java in PATH
if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="`which java`"
fi

if [ -z "$CLASSPATH" ]; then
    echo "You must set the CLASSPATH var" >&2
    exit 1
fi

CLASSPATH=$CLASSPATH./impl/target/sstable-generator-impl.jar:./generator/target/sstable-generator.jar:./cassandra-4/target/sstable-generator-cassandra-4.jar

set +x

java $JAVA_AGENT -cp "$CLASSPATH" $JVM_OPTS \
    com.instaclustr.sstable.generator.LoaderApplication \
    fixed \
    --keyspace test \
    --table test \
    --output-dir=/home/smikloso/output \
    --cassandra-version=4 \
    --schema cassandra-3/src/test/resources/cassandra/cql/table.cql \
    --threads 1
