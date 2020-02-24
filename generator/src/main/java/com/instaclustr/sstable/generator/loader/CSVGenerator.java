package com.instaclustr.sstable.generator.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.MappedRow;
import com.instaclustr.sstable.generator.RowMapper;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class CSVGenerator implements Generator {

    private final SSTableGenerator ssTableGenerator;
    private final Supplier<CsvListReader> csvListReader;

    public CSVGenerator(final SSTableGenerator ssTableGenerator,
                        final Supplier<CsvListReader> csvListReader) {
        this.ssTableGenerator = ssTableGenerator;
        this.csvListReader = csvListReader;
    }

    @Override
    public void generate(final RowMapper rowMapper) throws SSTableGeneratorException {
        try (final CsvListReader csvReader = this.csvListReader.get()) {
            ssTableGenerator.generate(new CSVLoaderIterator(csvReader, rowMapper));
        } catch (final Exception ex) {
            throw new SSTableGeneratorException("Unable to generate SSTables from CSV.", ex);
        }
    }

    public static class CsvListReaderFactory {

        public static CsvListReader fromFile(final String csvFile) throws Exception {
            return new CsvListReader(new BufferedReader(new FileReader(csvFile)), CsvPreference.STANDARD_PREFERENCE);
        }

        public static CsvListReader fromFile(final File csvFile) throws Exception {
            return new CsvListReader(new BufferedReader(new FileReader(csvFile)), CsvPreference.STANDARD_PREFERENCE);
        }
    }

    static class CSVLoaderIterator implements Iterator<MappedRow> {

        private final CsvListReader csvListReader;
        private final RowMapper rowMapper;
        private List<String> lastReadRow;

        public CSVLoaderIterator(final CsvListReader csvReader,
                                 final RowMapper rowMapper) {
            this.csvListReader = csvReader;
            this.rowMapper = rowMapper;
        }

        @Override
        public boolean hasNext() {
            try {
                lastReadRow = csvListReader.read();

                return lastReadRow == null;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to determine if there is next row!", e);
            }
        }

        @Override
        public MappedRow next() {
            return new MappedRow(rowMapper.map(lastReadRow));
        }
    }
}
