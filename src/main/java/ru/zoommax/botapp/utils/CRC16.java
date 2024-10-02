package ru.zoommax.botapp.utils;

public class CRC16 {
    private static final int POLYNOMIAL = 0x1021; // Полином CRC-16-CCITT
    private static final int PRESET_VALUE = 0xFFFF; // Начальное значение

    public static String calculateCRC16(byte[] data) {
        int crc = PRESET_VALUE;

        for (byte b : data) {
            crc ^= (b << 8);

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
        }

        crc &= 0xFFFF; // Ограничиваем результат до 16 бит

        // Возвращаем строку в шестнадцатеричном формате
        return String.format("%04X", crc);
    }
}
