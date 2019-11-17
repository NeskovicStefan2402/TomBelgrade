package com.example.tombelgradeapp.rest;

public class BlepUtils {
    public static final String BLEP_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String nRF_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String nRF_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    //public static String nRF_RX_Unknown1 = "6e400004-b5a3-f393-e0a9-e50e24dcca9e";
    //public static String nRF_RX_Unknown2 = "6e400005-b5a3-f393-e0a9-e50e24dcca9e";

    public static final int _UINT8 = 2;
    public static final int _UINT16 = 4;
    public static final int _UINT32 = 8;

    public static Integer getIntValue(byte[] value, int format, int position) {
        if (value == null)
            return null;
        if ((position + format) > value.length)
            return null;
        switch (format) {

            case _UINT8:
                return Integer.valueOf(add(value[position], value[(position + 1)]));

            case _UINT16:
                return Integer.valueOf(add(value[position], value[(position + 1)],
                        value[(position + 2)], value[(position + 3)]));

            case _UINT32:
                return Integer.valueOf(add(value[position], value[(position + 1)],
                        value[(position + 2)], value[(position + 3)]));
        }
        return null;
    }



    private static int add(byte byte1, byte byte2) {
        return (byte2 & 0xF) + ((byte1 & 0xF) << 4);
    }

    private static int add(byte byte1, byte byte2, byte byte3, byte byte4) {
        return (byte4 & 0xF) + ((byte3 & 0xF) << 4) + ((byte2 & 0xF) << 8)
                + ((byte1 & 0xF) << 12);
    }

    private static int signed(int value, int length) {
        if ((value & 1 << length - 1) != 0)
            value = -1 * ((1 << length - 1) - (value & (1 << length - 1) - 1));
        return value;
    }

}
