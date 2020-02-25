package com.instaclustr.sstable.generator.loader;

import java.util.Iterator;

import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.MappedRow;
import com.instaclustr.sstable.generator.RowMapper;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;

public class RandomGenerator implements Generator {

    private final SSTableGenerator ssTableGenerator;
    private final int numberOfRecords;

    public RandomGenerator(final SSTableGenerator ssTableGenerator, int numberOfRecords) {
        this.ssTableGenerator = ssTableGenerator;
        this.numberOfRecords = numberOfRecords;
    }

    @Override
    public void generate(final RowMapper rowMapper) {
        try {
            ssTableGenerator.generate(new RandomIterator(rowMapper, numberOfRecords));
        } catch (final Exception ex) {
            throw new SSTableGeneratorException("Unable to generate SSTables from RandomIterator.", ex);
        }
    }

    static class RandomIterator implements Iterator<MappedRow> {

        private final int numberOfRecords;
        private final RowMapper rowMapper;
        private int currentRecords = 0;

        public RandomIterator(final RowMapper rowMapper,
                              final int numberOfRecords) {
            this.numberOfRecords = numberOfRecords;
            this.rowMapper = rowMapper;
        }

        @Override
        public boolean hasNext() {
            return currentRecords++ < numberOfRecords;
        }

        @Override
        public MappedRow next() {

            if (currentRecords % 10000 == 0) {
                System.out.println(currentRecords);
            }

            return new MappedRow(rowMapper.random());
        }
    }
}
