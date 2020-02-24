package com.instaclustr.sstable.generator;

public interface Generator {

    void generate(final RowMapper rowMapper);
}
