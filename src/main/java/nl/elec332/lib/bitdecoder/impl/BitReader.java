package nl.elec332.lib.bitdecoder.impl;

import nl.elec332.lib.bitdecoder.api.IBitReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elec332 on 28-4-2020
 */
public class BitReader implements IBitReader {

    public BitReader(byte[] data, int startIndex) {
        this.data = data;
        this.byteIndex = startIndex;
        this.bitIndex = 0;
        this.currentByte = Byte.toUnsignedInt(data[startIndex]);
        this.blocked = false;
        this.properties = new HashMap<>();
    }

    private static final int[] PADDING;

    private final byte[] data;
    private final Map<String, Object> properties;

    private int byteIndex;
    private int currentByte;
    private int bitIndex;
    private boolean blocked;

    @Override
    public int getBitIndex() {
        return bitIndex;
    }

    @Override
    public int getByteIndex() {
        return byteIndex;
    }

    @Override
    public void setProperty(String name, Object obj) {
        properties.put(name, obj);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public short readByte() {
        return readFewBits_(8);
    }

    @Override
    public boolean isNextByteMatch(int value) {
        if (blocked) {
            return false;
        }
        if (peekBytesThrowing(1)[0] == value) {
            readByte();
            return true;
        }
        return false;
    }

    @Override
    public byte[] peekBytes(int bytes) {
        return peekBytes(bytes, false);
    }

    @Override
    public byte[] peekBytesThrowing(int bytes) {
        return peekBytes(bytes, true);
    }

    @Override
    public int getCurrentByte() {
        return currentByte;
    }

    private byte[] peekBytes(int bytes, boolean throwing) {
        checkBlocked();
        byte[] ret = new byte[bytes];
        if (bitIndex != 0) {
            if (throwing) {
                throw new IllegalStateException();
            } else {
                System.out.println("WARNING: bitindex is " + bitIndex);
            }
        }
        System.arraycopy(data, byteIndex, ret, 0, bytes);
        return ret;
    }

    @Override
    public byte readFewBits(int bits) {
        if (bits > 6) {
            throw new IllegalArgumentException("Too many bits, try another method");
        }
        return (byte) readFewBits_(bits);
    }

    @Override
    public void finishByte() {
        if (bitIndex == 0) {
            return;
        }
        checkBlocked();
        bitIndex = 8;
        nextByte();
    }

    private short readFewBits_(int bits) {
        checkBlocked();
        if (bits > 8) {
            throw new IllegalArgumentException("Too many bits, try another method");
        }
        int diff = 8 - bitIndex;
        int ret;
        if (diff >= bits) {
            ret = currentByte >> (diff - bits);
            bitIndex += bits;
        } else {
            int offset = bits - diff;
            ret = currentByte << offset;
            bitIndex += diff;
            nextByte();
            ret |= currentByte >> (8 - offset);
            bitIndex += offset;
        }
        checkNextByte();
        return (short) (ret & PADDING[bits]);
    }

    private void checkNextByte() {
        checkBlocked();
        if (bitIndex > 7) {
            nextByte();
        }
    }

    private void nextByte() {
        if (bitIndex != 8) {
            throw new IllegalStateException();
        }
        checkBlocked();
        byteIndex++;
        bitIndex = 0;
        if (byteIndex >= data.length) {
            blocked = true;
            currentByte = -1;
            return;
        }
        currentByte = Byte.toUnsignedInt(data[byteIndex]);
    }

    private void checkBlocked() {
        if (blocked) {
            throw new IllegalStateException();
        }
    }

    static {
        PADDING = new int[]{
                0b0, 0b1, 0b11, 0b111, 0b1111, 0b11111, 0b111111, 0b1111111, 0b11111111
        };
    }

}
