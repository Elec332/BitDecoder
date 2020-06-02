package nl.elec332.lib.bitdecoder.api;

/**
 * Created by Elec332 on 6-5-2020
 * <p>
 * Read individual bits from a byte-array
 */
public interface IBitReader {

    /**
     * Gets the bit index of the current byte
     *
     * @return The bit index of the current byte
     */
    int getBitIndex();

    /**
     * Gets the current byte-index of the byte-array
     *
     * @return The current byte-index of the byte-array
     */
    int getByteIndex();

    /**
     * Set a property on this reader
     *
     * @param name The name of the property
     * @param obj  The property object / value
     */
    void setProperty(String name, Object obj);

    /**
     * Gets the value of a property
     *
     * @param name The name of the property
     * @return The value of the given property
     */
    Object getProperty(String name);

    /**
     * Reads a single bit (1 or 0, true or false)
     *
     * @return The next bit
     */
    default boolean readBit() {
        return readFewBits(1) == 1;
    }

    /**
     * Reads a single unsigned byte (8 bits)
     *
     * @return The unsigned value for the next 8 bits
     */
    short readByte();

    /**
     * Checks if the next byte matches the provided value
     * The current bit-index must be zero when running this method!
     *
     * @param value The 8-bit value to be checked
     * @return Whether the next byte matches the given value
     */
    boolean isNextByteMatch(int value);

    /**
     * Peeks the next few bytes. Does not enforce the bit-index being zero.
     *
     * @param bytes The amount of bytes to peek
     * @return The peeked bytes
     */
    byte[] peekBytes(int bytes);

    /**
     * Peeks the next few bytes. Enforces the bit-index being zero.
     *
     * @param bytes The amount of bytes to peek
     * @return The peeked bytes
     * @throws IllegalStateException When the current bit-index is not zero
     */
    byte[] peekBytesThrowing(int bytes) throws IllegalStateException;

    /**
     * Returns the byte that is currently being read
     *
     * @return The byte that is currently being read
     */
    int getCurrentByte();

    /**
     * Reads multiple (signed) bytes
     *
     * @param bytes The amount of bytes to be read
     * @return The amount of bytes requested
     */
    default byte[] readBytes(int bytes) {
        byte[] ret = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            ret[i] = (byte) readByte();
        }
        return ret;
    }

    /**
     * Read a few {@code <= 6} bits
     *
     * @param bits The amount of bits to be read
     * @return The requested bits
     */
    byte readFewBits(int bits);

    /**
     * Reads multiple {@code <= 12} bits as a short
     *
     * @param bits The amount of bits to be read
     * @return The requested bits
     */
    default short readShortBits(int bits) {
        if (bits > 12) {
            throw new IllegalArgumentException("Too many bits, try another method");
        }
        return (short) readBits(bits);
    }

    /**
     * Reads multiple {@code <= 24} bits as a int
     *
     * @param bits The amount of bits to be read
     * @return The requested bits
     */
    default int readBits(int bits) {
        if (bits > 24) {
            throw new IllegalArgumentException("Too many bits, try another method");
        }
        return (int) readManyBits(bits);
    }

    /**
     * Reads multiple {@code <= 48} bits as a long
     *
     * @param bits The amount of bits to be read
     * @return The requested bits
     */
    default long readManyBits(int bits) {
        if (bits > 48) {
            throw new IllegalArgumentException("Too many bits, try another method");
        }
        long ret = 0;

        int times = Math.floorDiv(bits, 8);
        for (int i = 0; i < times; i++) {
            ret |= readByte() << (bits - (i * 8));
        }
        if (bits % 8 > 0) {
            ret |= readFewBits(bits % 8);
        }

        return ret;
    }

    /**
     * Finishes reading the current byte and moves on to the next one
     * Basically: bitIndex = 0; byteIndex++;
     */
    void finishByte();

}
