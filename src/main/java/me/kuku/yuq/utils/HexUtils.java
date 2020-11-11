package me.kuku.yuq.utils;

public class HexUtils {

    public static String bytesToHexString(byte[] src){
        StringBuilder sb = new StringBuilder();
        for (byte element: src){
            int v = (int) element & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2){
                sb.append(0);
            }
            sb.append(hv);
        }
        return sb.toString();
    }
}
