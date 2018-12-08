package blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static final int SEED = 2385298;

    public static final byte[] ACCOUNT_ZERO = new byte[32];

    public static final String ACCOUNT_ZERO_AS_STRING = bytesToHexString(ACCOUNT_ZERO);

    public static final long REWARD = 1000;

    public static final int MAX_CHAIN_LENGTH = 20;

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

    public static Map<String, Long> currentAccountBalances(Chain chain) {

        Map<String, Long> accountBalances = new HashMap<>();

        chain.getBlocks().forEach(b -> b.getTransactions().forEach(t -> {

            long amount = t.getAmount();
            String from = t.getFromAsString();
            String to = t.getToAsString();

            if (!Utils.ACCOUNT_ZERO_AS_STRING.equals(from)) {
                long balanceFrom = accountBalances.get(from);
                accountBalances.put(from, balanceFrom - amount);
            }
            accountBalances.putIfAbsent(to, 0L);
            long balanceTo = accountBalances.get(to);
            accountBalances.put(to, balanceTo + amount);
        }));
        return accountBalances;
    }

    public static void printAccountBalances(Chain chain) {
        int[] total = new int[] {0};
        currentAccountBalances(chain).forEach((acc, amt) -> {
            System.out.println("Account: " + acc + " balance: " + amt);
            total[0] += amt;
        });
        System.out.println("Total: " + total[0]);
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
