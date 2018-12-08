package blockchain;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class Transaction {

    public static final int TRANSACTION_SIZE = 32 + 32 + 8;

    private static final int ADDRESS_SIZE = 32;

    private byte[] from;
    private byte[] to;
    private long amount;

    public Transaction(String from, String to, long amount) {
        this(Utils.hexStringToByteArray(from), Utils.hexStringToByteArray(to), amount);
    }

    public Transaction(byte[] from, byte[] to, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        } else if (from.length != ADDRESS_SIZE) {
            throw new IllegalArgumentException("'from' size is not " + ADDRESS_SIZE + " bytes");
        } else if (to.length != ADDRESS_SIZE) {
            throw new IllegalArgumentException("'to' size is not " + ADDRESS_SIZE + " bytes");
        }
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Transaction(byte[] transaction) {
        if (transaction.length != TRANSACTION_SIZE) {
            throw new IllegalArgumentException();
        }
        ByteBuffer b = ByteBuffer.wrap(transaction);
        from = new byte[32];
        b.get(from, 0, 32);
        to = new byte[32];
        b.get(to, 0, 32);
        amount = b.getLong();
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    public byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(TRANSACTION_SIZE);
        b.put(from);
        b.put(to);
        b.putLong(amount);
        if (amount <= 0) {
            throw new IllegalArgumentException();
        }
        return b.array();
    }

    public byte[] getFrom() {
        return from;
    }

    public byte[] getTo() {
        return to;
    }

    public String getFromAsString() {
        return Utils.bytesToHexString(from);
    }

    public String getToAsString() {
        return Utils.bytesToHexString(to);
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return amount == that.amount &&
                Arrays.equals(from, that.from) &&
                Arrays.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(amount);
        result = 31 * result + Arrays.hashCode(from);
        result = 31 * result + Arrays.hashCode(to);
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "from=" + getFromAsString() +
                ", to=" + getToAsString() +
                ", amount=" + amount +
                '}';
    }
}
