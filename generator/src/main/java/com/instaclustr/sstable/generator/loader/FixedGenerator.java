package com.instaclustr.sstable.generator.loader;

import static java.util.stream.Collectors.toList;

import java.util.List;
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
            final List<List<Object>> listOfRows = rowMapper.get();

            if (listOfRows != null) {
                ssTableGenerator.generate(rowMapper.get().stream().filter(Objects::nonNull).map(MappedRow::new).collect(toList()).iterator());
            }
        } catch (final Exception ex) {
            throw new SSTableGeneratorException("Unable to generate SSTables from FixedLoader.", ex);
        }
    }
}
