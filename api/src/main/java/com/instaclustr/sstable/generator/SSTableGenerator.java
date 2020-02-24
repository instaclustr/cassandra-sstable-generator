package com.instaclustr.sstable.generator;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;

public abstract class SSTableGenerator {

    protected BulkLoaderSpec spec;

    protected RowMapper rowMapper;

    public abstract void generate(final Iterator<MappedRow> rowsIterator);

    public abstract Object getWriter(BulkLoaderSpec spec, RowMapper rowMapper);

    public SSTableGenerator withBulkLoaderSpec(BulkLoaderSpec spec) {
        this.spec = spec;
        return this;
    }

    public SSTableGenerator withRowMapper(RowMapper rowMapper) {
        this.rowMapper = rowMapper;
        return this;
    }

    public void validate() {
        requireNonNull(spec, "spec has to be specified!");
        requireNonNull(rowMapper, "rowMapper has to be specified!");
    }
}
