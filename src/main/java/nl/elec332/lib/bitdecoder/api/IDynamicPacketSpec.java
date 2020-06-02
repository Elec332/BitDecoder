package nl.elec332.lib.bitdecoder.api;

import nl.elec332.lib.java.tree.INamedTreePart;

import java.util.function.*;

/**
 * Created by Elec332 on 29-4-2020
 * <p>
 * Dynamic (sub)packet specification
 */
public interface IDynamicPacketSpec {

    /**
     * Whether this (sub)packet spec has a name
     *
     * @return Whether this (sub)packet spec has a name
     */
    boolean hasName();

    /**
     * Returns the name of this (sub)packet spec
     *
     * @return The name of this (sub)packet spec
     */
    String getName();

    /**
     * Decodes this (sub)packet spec given a byte array
     *
     * @param data The data
     * @return The decoded (sub)packet data
     */
    default INamedTreePart decode(byte[] data) {
        return decode(data, 0);
    }

    /**
     * Decodes this (sub)packet spec given a byte array and a byte-index
     *
     * @param data The data
     * @param byteIndex The index from which to start reading
     * @return The decoded (sub)packet data
     */
    INamedTreePart decode(byte[] data, int byteIndex);

    /**
     * Decodes this (sub)packet spec from a {@link IBitReader}
     *
     * @param reader The reader
     * @return The decoded (sub)packet data
     */
    INamedTreePart decode(IBitReader reader);

    /**
     * Decodes this (sub)packet spec from a {@link IBitReader} to the provided {@link INamedTreePart}
     *
     * @param reader The bit reader
     * @param tree The tree to write the data to
     */
    void decode(IBitReader reader, INamedTreePart tree);

    /**
     * Builder for a (sub)packet specification
     */
    interface Builder {

        /**
         * Sets the name of this (sub)packet spec
         *
         * @param name The name of this (sub)packet spec
         * @return The current builder
         */
        Builder setName(String name);

        /**
         * Imports another builder (with all its parameters) into this builder
         *
         * @param other The builder to be imported
         * @return The current builder
         */
        Builder with(Builder other);

        /**
         * Runs the provided {@link Consumer} on this builder
         *
         * @param consumer The {@link Consumer} to be used on this builder
         * @return The current builder
         */
        default Builder with(Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }

        /**
         * Imports another specification (with all its parameters) into this builder
         *
         * @param spec The specification to be imported
         * @return The current builder
         */
        default Builder importParameters(IDynamicPacketSpec spec) {
            return addSpecialParameter(spec::decode);
        }

        /**
         * Discards the amount of bytes returned by the parameter.
         * The current byte will be finished first
         *
         * @param amount A function the will return the amount of bytes to discard
         *               based on the current {@link IBitReader}
         * @return The current builder
         */
        default Builder discardBytes(ToIntFunction<IBitReader> amount) {
            return discardBytes((reader, tree) -> amount.applyAsInt(reader));
        }

        /**
         * Discards the amount of bytes returned by the parameter.
         * The current byte will be finished first
         *
         * @param amount A function the will return the amount of bytes to discard
         *               based on the current {@link IBitReader} and the current data tree
         * @return The current builder
         */
        default Builder discardBytes(ToIntBiFunction<IBitReader, INamedTreePart> amount) {
            return addSpecialParameter((reader, tree) -> {
                reader.finishByte();
                reader.readBytes(amount.applyAsInt(reader, tree));
            });
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(Predicate<IBitReader> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(Predicate<IBitReader> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            return addChoiceParameters(null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(BiPredicate<IBitReader, INamedTreePart> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            return addChoiceParameters(null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Predicate<IBitReader> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(name, null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(name, null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Predicate<IBitReader> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            return addChoiceParameters(name, null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, BiPredicate<IBitReader, INamedTreePart> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            return addChoiceParameters(name, null, predicate, whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param namer A function to determine a value for the name based on the outcome of the predicate
         *              EG: boolean ? "Present" : "Not Present"
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Function<Boolean, Object> namer, Predicate<IBitReader> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(name, namer, (reader, tree) -> predicate.test(reader), whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param namer A function to determine a value for the name based on the outcome of the predicate
         *              EG: boolean ? "Present" : "Not Present"
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link Consumer} to run for this builder when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link Consumer} to run for this builder when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Function<Boolean, Object> namer, BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> whenTrue, Consumer<Builder> whenFalse) {
            return addChoiceParameters(name, namer, predicate,
                    whenTrue == null ? null : newInstance().with(whenTrue).build(),
                    whenFalse == null ? null : newInstance().with(whenFalse).build());
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param namer A function to determine a value for the name based on the outcome of the predicate
         *              EG: boolean ? "Present" : "Not Present"
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Function<Boolean, Object> namer, Predicate<IBitReader> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            return addChoiceParameters(name, namer, (reader, tree) -> predicate.test(reader), whenTrue, whenFalse);
        }

        /**
         * Adds a branch to the reader
         * When the predicate returns true, the first branch will be followed.
         * The second branch will be followed if it returns false.
         *
         * @param name The name for this branch
         * @param namer A function to determine a value for the name based on the outcome of the predicate
         *              EG: boolean ? "Present" : "Not Present"
         * @param predicate The predicate used to determine which branch to take
         * @param whenTrue The {@link IDynamicPacketSpec} specification to decode when the predicate returns true,
         *                 can be null.
         * @param whenFalse The {@link IDynamicPacketSpec} specification to decode when the predicate returns false,
         *                 can be null.
         * @return The current builder
         */
        default Builder addChoiceParameters(String name, Function<Boolean, Object> namer, BiPredicate<IBitReader, INamedTreePart> predicate, IDynamicPacketSpec whenTrue, IDynamicPacketSpec whenFalse) {
            if (name != null && name.isEmpty()) {
                throw new IllegalArgumentException("Empty name!");
            }
            return addSpecialParameter((reader, tree) -> {
                boolean b = predicate.test(reader, tree);
                if (name != null) {
                    tree.put(name, namer == null ? b : namer.apply(b));
                }
                IDynamicPacketSpec choice = b ? whenTrue : whenFalse;
                if (choice != null) {
                    if (choice.hasName()) {
                        tree.put(choice.getName(), choice.decode(reader));
                    } else {
                        choice.decode(reader, tree);
                    }
                }
            });
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name of the specification.
         * If the name of the specification is not defined, all parameters
         * from the specification will be imported.
         *
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(IDynamicPacketSpec decoder) {
            if (decoder.hasName()) {
                return addNestedParameter((reader, tree) -> true, decoder);
            }
            return importParameters(decoder);
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name of the specification.
         *
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         * @throws IllegalArgumentException When the decoder does not have a name
         */
        default Builder addNestedParameter(Predicate<IBitReader> predicate, IDynamicPacketSpec decoder) {
            return addNestedParameter((reader, tree) -> predicate.test(reader), decoder);
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name of the specification.
         *
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         * @throws IllegalArgumentException When the decoder does not have a name
         */
        default Builder addNestedParameter(BiPredicate<IBitReader, INamedTreePart> predicate, IDynamicPacketSpec decoder) {
            if (!decoder.hasName()) {
                throw new IllegalArgumentException();
            }
            return addNestedParameter(decoder.getName(), predicate, decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification will be added as a nested parameter.
         *
         * The specification must be given a name, or this method will throw an {@link IllegalArgumentException}
         *
         * @param decoder The specification to add to this builder
         * @return The current builder
         * @throws IllegalArgumentException When the resulting specification does not have a name
         */
        default Builder addNestedParameter(Consumer<Builder> decoder) {
            return addNestedParameter((reader) -> true, decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification will be added as a nested parameter.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * The specification must be given a name, or this method will throw an {@link IllegalArgumentException}
         *
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         * @throws IllegalArgumentException When the resulting specification does not have a name
         */
        default Builder addNestedParameter(Predicate<IBitReader> predicate, Consumer<Builder> decoder) {
            return addNestedParameter((reader, tree) -> predicate.test(reader), decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification will be added as a nested parameter.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * The specification must be given a name, or this method will throw an {@link IllegalArgumentException}
         *
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         * @throws IllegalArgumentException When the resulting specification does not have a name
         */
        default Builder addNestedParameter(BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> decoder) {
            return addNestedParameter(predicate, newInstance()
                    .with(decoder)
                    .build());
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name provided.
         *
         * @param name The name to use for this nested parameter
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, IDynamicPacketSpec decoder) {
            return addNestedParameter(name, (reader, tree) -> true, decoder);
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name provided
         *
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name to use for this nested parameter
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, BiPredicate<IBitReader, INamedTreePart> predicate, IDynamicPacketSpec decoder) {
            return addParameter(name, (reader, tree) -> {
                if (predicate.test(reader, tree)) {
                    return decoder.decode(reader);
                }
                return null;
            });
        }

        /**
         * Adds an already defined {@link IDynamicPacketSpec} specification as
         * a parameter to this builder using the name provided
         *
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name to use for this nested parameter
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, Predicate<IBitReader> predicate, IDynamicPacketSpec decoder) {
            return addNestedParameter(name, (reader, tree) -> predicate.test(reader), decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification
         * will be added as a nested parameters using the name provided.
         *
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, Consumer<Builder> decoder) {
            return addNestedParameter(name, (reader, tree) -> true, decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification
         * will be added as a nested parameter using the name provided.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name to use for this nested parameter
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, Predicate<IBitReader> predicate, Consumer<Builder> decoder) {
            return addNestedParameter(name, (reader, tree) -> predicate.test(reader), decoder);
        }

        /**
         * Runs the provided {@link Consumer} on a new builder.
         * The new builder will then be build and the resulting specification
         * will be added as a nested parameter using the name provided.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name to use for this nested parameter
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The specification to add to this builder
         * @return The current builder
         */
        default Builder addNestedParameter(String name, BiPredicate<IBitReader, INamedTreePart> predicate, Consumer<Builder> decoder) {
            return addNestedParameter(name, predicate, newInstance()
                    .with(decoder)
                    .build());
        }

        /**
         * Adds a parameter to this builder
         * The provided name will be the name of the property,
         * and the decoder will be used to determine the value.
         *
         * @param name The name of the property
         * @param decoder The decoder that will be used to determine the value
         * @param <T> The type of the value
         * @return The current builder
         */
        default <T> Builder addParameter(String name, Function<IBitReader, T> decoder) {
            return addParameter(name, (reader, tree) -> decoder.apply(reader));
        }

        /**
         * Adds a parameter to this builder
         * The provided name will be the name of the property,
         * and the decoder will be used to determine the value.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name of the property
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The decoder that will be used to determine the value
         * @param <T> The type of the value
         * @return The current builder
         */
        default <T> Builder addParameter(String name, Predicate<IBitReader> predicate, Function<IBitReader, T> decoder) {
            return addParameter(name, (reader, tree) -> predicate.test(reader), (reader, tree) -> decoder.apply(reader));
        }

        /**
         * Adds a parameter to this builder
         * The provided name will be the name of the property,
         * and the decoder will be used to determine the value.
         * If the predicate returns false, the parameter will be skipped during the decoding process
         *
         * @param name The name of the property
         * @param predicate The predicate used to check whether to decode the parameter or not
         * @param decoder The decoder that will be used to determine the value (comes with read access to the current tree)
         * @param <T> The type of the value
         * @return The current builder
         */
        default <T> Builder addParameter(String name, BiPredicate<IBitReader, INamedTreePart> predicate, BiFunction<IBitReader, INamedTreePart, T> decoder) {
            return addParameter(name, (reader, tree) -> {
                if (predicate.test(reader, tree)) {
                    return decoder.apply(reader, tree);
                }
                return null;
            });
        }

        /**
         * Adds a parameter to this builder
         * The provided name will be the name of the property,
         * and the decoder will be used to determine the value.
         *
         * @param name The name of the property
         * @param decoder The decoder that will be used to determine the value (comes with read access to the current tree)
         * @param <T> The type of the value
         * @return The current builder
         */
        default <T> Builder addParameter(String name, BiFunction<IBitReader, INamedTreePart, T> decoder) {
            return addSpecialParameter((reader, tree) -> {
                Object o = decoder.apply(reader, tree.getImmutable());
                tree.put(name, o);
            });
        }

        /**
         * Reads data from the provided reader without processing it
         * Can be used for skipping reserved bits or skipping unimplemented parts of a packet
         *
         * @param consumer The data reader
         * @return The current builder
         */
        default Builder readData(Consumer<IBitReader> consumer) {
            return addSpecialParameter(((reader, tree) -> consumer.accept(reader)));
        }

        /**
         * Asserts the state of the {@link IBitReader}
         *
         * @param check The checker
         * @param errorMessage An error message in case the check fails
         * @return The current builder
         */
        default Builder assertReader(Predicate<IBitReader> check, String errorMessage) {
            return addSpecialParameter((reader, tree) -> {
                if (!check.test(reader)) {
                    throw new AssertionError(errorMessage);
                }
            });
        }

        /**
         * Asserts the state of the data currently in the parameter tree
         *
         * @param check The checker
         * @param errorMessage An error message in case the check fails
         * @return The current builder
         */
        default Builder assertData(Predicate<Function<String, Object>> check, String errorMessage) {
            return addSpecialParameter((reader, tree) -> {
                if (!check.test(tree::get)) {
                    throw new AssertionError(errorMessage);
                }
            });
        }

        /**
         * Asserts the state of the last object that has been modified in the tree
         *
         * @param check The checker
         * @param errorMessage An error message in case the check fails
         * @return The current builder
         */
        default Builder assertPreviousParameter(Predicate<Object> check, String errorMessage) {
            return addSpecialParameter((reader, tree) -> {
                if (!check.test(tree.get(tree.getLastModifiedObject()))) {
                    throw new AssertionError(errorMessage);
                }
            });
        }

        /**
         * Adds a special parameter.
         * Provides free access to the {@link IBitReader} and the tree
         *
         * @param decoder The decoder
         * @return The current builder
         */
        default Builder addSpecialParameter(BiConsumer<IBitReader, INamedTreePart> decoder) {
            return shouldContinue((reader, tree) -> {
                decoder.accept(reader, tree);
                return true;
            });
        }

        /**
         * Runs the provided checker to decide if the decoding process should continue or not
         *
         * @param checker The checker
         * @return The current builder
         */
        Builder shouldContinue(BiPredicate<IBitReader, INamedTreePart> checker);

        /**
         * Creates a copy of this builder
         *
         * @return A copy of this builder
         */
        Builder copy();

        /**
         * Builds this builder into a {@link IDynamicPacketSpec} specification
         *
         * @return The {@link IDynamicPacketSpec} specification from this builder
         */
        IDynamicPacketSpec build();

        /**
         * Created a new builder
         *
         * @return A new builder
         */
        Builder newInstance();

    }

}
