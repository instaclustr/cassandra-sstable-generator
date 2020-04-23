package com.instaclustr.sstable.generator;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RowMapper1 implements RowMapper {

    public static final String KEYSPACE = "test";
    public static final String TABLE = "test";

    @Override
    public List<Object> map(final List<String> row) {
        return null;
    }

    @Override
    public Stream<List<Object>> get() {
        return Stream.generate((Supplier<List<Object>>) () -> new ArrayList<Object>() {{
            add(UUID.randomUUID());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
        }}).limit(10);
    }

    @Override
    public List<Object> random() {
        return new ArrayList<Object>() {{
            add(UUID.randomUUID());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
        }};
    }

    @Override
    public String insertStatement() {
        return format("INSERT INTO %s.%s (id, name, surname, description, profession) VALUES (?, ?, ?, ?, ?);", KEYSPACE, TABLE);
    }
}
