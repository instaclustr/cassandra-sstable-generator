#!/bin/bash

java -Xmx8192m -Xms8192m \
    -cp impl/target/sstable-generator-impl-1.0.jar:generator/target/sstable-generator-1.0.jar:cassandra-4/target/cassandra-4-1.0.jar \
    com.instaclustr.sstable.generator.LoaderApplication \
    fixed \
    --keyspace test \
    --table test \
    --output-dir=/home/smikloso/output \
    --schema cassandra-3/src/test/resources/cassandra/cql/table.cql \
    --threads 1
