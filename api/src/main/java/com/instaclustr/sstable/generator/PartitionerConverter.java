package com.instaclustr.sstable.generator;

import picocli.CommandLine;

public abstract class PartitionerConverter<T> implements CommandLine.ITypeConverter<T> {

    public enum Partitioner {
        MURMUR("murmur");

        private final String name;

        Partitioner(final String name) {
            this.name = name;
        }

        public static Partitioner parse(final String name) {
            if (name == null) {
                return null;
            }

            for (final Partitioner p : Partitioner.values()) {
                if (p.name.equals(name.toLowerCase())) {
                    return p;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return "Partitioner{" +
                    "name='" + name + '\'' +
                    "} " + super.toString();
        }
    }
}
