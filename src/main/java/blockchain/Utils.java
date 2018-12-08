package blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Utils {

    public static final short DIFFICULTY = 4;

    public static final short MAX_TRANSACTIONS_PER_BLOCK = 5;
    
    public static final int BLOCKSIZE = 8 + 32 + 32 + 8 + 8 + 4 + 4 + MAX_TRANSACTIONS_PER_BLOCK * Transaction.TRANSACTION_SIZE;
    
    public static final int NONCE_OFFSET = 40;

    public static String bytesToHexString(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] hexStringToByteArray(String hashString) {
        int length = hashString.length();
        if (length % 2 != 0 ) {
            throw new IllegalArgumentException("length must be even");
        }
        byte[] hash = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            hash[i / 2] = (byte) ((Character.digit(hashString.charAt(i), 16) << 4)
                                    + Character.digit(hashString.charAt(i+1), 16));
        }
        return hash;
    }

    public static MessageDigest getSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
