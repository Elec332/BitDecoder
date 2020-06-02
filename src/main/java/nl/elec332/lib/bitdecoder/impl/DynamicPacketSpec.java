package nl.elec332.lib.bitdecoder.impl;

import nl.elec332.lib.bitdecoder.api.IBitReader;
import nl.elec332.lib.bitdecoder.api.IDynamicPacketSpec;
import nl.elec332.lib.java.tree.INamedTreePart;
import nl.elec332.lib.java.tree.NamedTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 29-4-2020
 */
public class DynamicPacketSpec implements IDynamicPacketSpec {

    public static Builder builder() {
        return new SpecBuilder(new ArrayList<>());
    }

    private DynamicPacketSpec(List<BiPredicate<IBitReader, INamedTreePart>> readers, String name) {
        this.readers = List.copyOf(readers);
        this.name = name;
    }

    private final List<BiPredicate<IBitReader, INamedTreePart>> readers;
    private final String name;

    @Override
    public boolean hasName() {
        return name != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public INamedTreePart decode(byte[] data, int byteIndex) {
        return decode(new BitReader(data, byteIndex));
    }

    @Override
    public INamedTreePart decode(IBitReader reader) {
        final INamedTreePart tree = new NamedTree();
        decode(reader, tree);
        return tree;
    }

    @Override
    public void decode(IBitReader reader, INamedTreePart tree) {
        for (var r : readers) {
            if (!r.test(reader, tree)) {
                break;
            }
        }
    }

    private static class SpecBuilder implements Builder {

        private SpecBuilder(List<BiPredicate<IBitReader, INamedTreePart>> readers) {
            this.readers = readers;
        }

        private final List<BiPredicate<IBitReader, INamedTreePart>> readers;
        private String name = null;

        @Override
        public Builder setName(String name) {
            if (name == null || name.isEmpty()) {
                name = null;
            }
            this.name = name;
            return this;
        }

        @Override
        public Builder with(Builder other) {
            if (other instanceof SpecBuilder) {
                this.readers.addAll(((SpecBuilder) other).readers);
                return this;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder addNestedParameter(String name, BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> decoder) {
            Builder builder = builder();
            decoder.accept(builder);
            return addNestedParameter(name, predicate, builder.build());
        }

        @Override
        public Builder shouldContinue(BiPredicate<IBitReader, INamedTreePart> checker) {
            this.readers.add(checker);
            return this;
        }

        @Override
        public Builder copy() {
            return new SpecBuilder(new ArrayList<>(readers));
        }

        @Override
        public IDynamicPacketSpec build() {
            return new DynamicPacketSpec(readers, name);
        }

        @Override
        public Builder newInstance() {
            return builder();
        }

    }

}
