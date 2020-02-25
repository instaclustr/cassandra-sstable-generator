package com.instaclustr.sstable.generator.loader;

import java.util.Objects;

import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.MappedRow;
import com.instaclustr.sstable.generator.RowMapper;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;

public class FixedGenerator implements Generator {

    private final SSTableGenerator ssTableGenerator;

    public FixedGenerator(final SSTableGenerator ssTableGenerator) {
        this.ssTableGenerator = ssTableGenerator;
    }

    @Override
    public void generate(final RowMapper rowMapper) {
        try {
            ssTableGenerator.generate(rowMapper.get().filter(Objects::nonNull).map(MappedRow::new).iterator());
        } catch (final Exception ex) {
            throw new SSTableGeneratorException("Unable to generate SSTables from FixedLoader.", ex);
        }
    }
}
