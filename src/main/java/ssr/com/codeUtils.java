package ssr.com;

public class codeUtils {
    public static byte[] enCode(byte[] bytesIn){
        byte[] bytesOut = new byte[bytesIn.length];
        int i = 0;
        for(byte bt:bytesIn){
            bytesOut[i] = (byte)(bt+1);
            i++;
        }
        return bytesOut;
        //return bytesIn;
    }

    public static byte[] deCode(byte[] bytesIn){
        byte[] bytesOut = new byte[bytesIn.length];
        int i = 0;
        for(byte bt:bytesIn){
            bytesOut[i] = (byte)(bt-1);
            i++;
        }
        return bytesOut;
        //return bytesIn;
    }

    public static char enCodeChar(char charIn){
        char charOut = (char)((int)charIn+1);
        return charOut;
        //return charIn;
    }

    public static char deCodeChar(char charIn){
        char charOut = (char)((int)charIn-1);
        return charOut;
        //return charIn;
    }
}
