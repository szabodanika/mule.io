package common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class ByteUtils {


    /** Returns the index of the first byte of the first occurrence of the key in the source,
     * and returns -1 if the source does not contain the key
     * @param source
     * @param key
     * @return
     */

    public static int indexOf(byte[] source, byte[] key){
        byte[] buffer = new byte[key.length];
        for( int i = 0; i < source.length; i ++){
            for( int l = 0; l < buffer.length-1; l++){
                buffer[l] = buffer[l+1];
            }
            buffer[buffer.length-1] = source[i];

            if(Arrays.equals(key, buffer)) return i - (buffer.length-1);
        }
        return -1;
    }

    public static void messageInfo(Message message){
        System.out.println("MESSAGE ID LENGTH: " + message.getID().length);
        System.out.println("MESSAGE CONTENT LENGTH: " + message.getContent().length);
        System.out.println("MESSAGE ID SUM: " + ByteUtils.checkSum(message.getID()));
        System.out.println("MESSAGE CONTENT SUM: " + ByteUtils.checkSum(message.getContent()));
//        print("MESSAGE ID", message.getID());
//        print("MESSAGE CONTENT", message.getContent());
    }

    public static boolean isZeroArray(byte[] bytes){
        for (byte b : bytes) if(b != 0) return false;
        return true;
    }

    public static byte[] generateRandom(int length){
        byte[] bytes = new byte[length];
        for(int i = 0; i < length; i ++) {
            bytes[i] = (byte) new Random().nextInt(256);
        }
        return bytes;
    }

    public static byte[] generateRandomPositive(int length){
        byte[] bytes = new byte[length];
        for(int i = 0; i < length; i ++) {
            bytes[i] = (byte) new Random().nextInt(128);
        }
        return bytes;
    }

    public static byte[] intToBytes(int integer, int bytes) {
        return ByteBuffer.allocate(bytes).putInt(integer).array();
    }

    public static int bytesToInteger(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes);
        return wrapped.getInt();
    }

    public static byte[] Bytestobytes(Byte[] Bytes){
        byte[] bytes = new byte[Bytes.length];
        for (int i = 0; i < Bytes.length; i++) {
            bytes[i] = Bytes[i];
        }
        return bytes;
    }

    public static Byte[] bytestoBytes(byte[] bytes){
        Byte[] Bytes = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            Bytes[i] = bytes[i];
        }
        return Bytes;
    }

    public static byte[] cut(byte[] bytes, int start, int end){
        byte[] result = new byte[bytes.length-start-(bytes.length-end)];
        for(int i = 0; i < result.length; i++){
            result[i] = bytes[i+start];
        }
        return result;
    }

    public static int checkSum(byte[]... byteArrays){
        int sum = 0;
        for(byte[] bytes : byteArrays){
            for(byte b : bytes) sum += b;
        }
        return sum;
    }

    public static void print(String title, byte[] bytes){
        System.out.println("======== " + title + " (" + bytes.length+ "B) ========");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(" [" + i + " - " + bytes[i] +"]    ");
            if((i+1)%10==0) System.out.print("\n");
        }
        System.out.println("\n======== /" + title + " ========");
    }

    public static byte[] fillWithZeros(byte[] bytes, int length){
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++){
            result[i] = bytes.length < length ? bytes[i] : 0;
        }
        return result;
    }

    public static byte[] fillWithZerosToMultipleOf(byte[] bytes, int multipleOf){

        double a = bytes.length / multipleOf;
        int smallestFitting = (a - (int) a == 0 ? (int) a + 1 : (int) a) * multipleOf;

        return fillWithZeros(bytes, multipleOf);
    }

    public static void copy(byte[] bytes1, byte[] bytes2, int start1, int start2, int end1){
        for (int i = start1; i < end1; i++) {
            bytes2[start2+i] = bytes1[i];
        }
    }
}
