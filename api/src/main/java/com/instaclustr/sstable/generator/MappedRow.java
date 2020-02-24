package com.instaclustr.sstable.generator;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

public class MappedRow {
    public final List<Object> values;
    public final List<String> types;

    public MappedRow(final List<Object> values) {
        this.values = values;
        this.types = cellTypes(values);
    }

    /**
     * @param row list of objects representing a Cassandra row, column by column
     * @return list of strings where an item represents a type of such column
     */
    public List<String> cellTypes(final List<Object> row) {
        if (row == null) {
            return Collections.emptyList();
        }
        return row.stream().map(cell -> {
            if (cell != null) {
                return cell.getClass().toString();
            } else {
                return "unknown";
            }
        }).collect(toList());
    }
}
