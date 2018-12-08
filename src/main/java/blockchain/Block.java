package blockchain;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;

/*
    Block structure:
    --------------------------------------------------------------------------------------
    |Block number|prevHash|hash|nonce|timestamp|difficulty|noTransactions|transactions...|
    --------------------------------------------------------------------------------------

    Size in bytes:
    ---------------------
    |8|32|32|8|8|2|2|360|
    ---------------------

    Block structure for proof of work:
    ---------------------------------------------------------------------------------
    |Block number|prevHash|nonce|timestamp|difficulty|noTransactions|transactions...|
    ---------------------------------------------------------------------------------
    Size in bytes:
    -------------------------
    |8|32|8|8|2|2|(0-5) * 72|
    -------------------------
*/

public class Block {

    private long blockNumber;
    private Instant timestamp;

    private byte[] prevHash = new byte[32];
    private byte[] hash = new byte[32];

    private short difficulty;
    private long nonce;

    private short noTransactions;
    private List<Transaction> transactions;

    private transient boolean stop;

    public Block(long blockNumber, byte[] prevHash, short difficulty) {
        this.blockNumber = blockNumber;
        this.prevHash = prevHash;
        this.hash = new byte[32];
        this.nonce = 0;
        this.timestamp = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        this.difficulty = difficulty;
        this.noTransactions = 0;
        this.transactions = new ArrayList<>(Utils.MAX_TRANSACTIONS_PER_BLOCK);
    }

    public Block(long blockNumber, byte[] prevHash, short difficulty, Transaction... transactions) {
        this.blockNumber = blockNumber;
        this.prevHash = prevHash;
        this.hash = new byte[32];
        this.nonce = 0;
        this.timestamp = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        this.difficulty = difficulty;
        this.noTransactions = 0;
        this.transactions = new ArrayList<>(Utils.MAX_TRANSACTIONS_PER_BLOCK);
        for (Transaction t : transactions) {
            addTransaction(t);
        }

    }

    public Block(byte[] block) {
        if (block.length != Utils.BLOCKSIZE) {
            throw new IllegalArgumentException();
        }
        ByteBuffer b = ByteBuffer.wrap(block);
        blockNumber = b.getLong();
        b.get(prevHash, 0, 32);
        b.get(hash, 0, 32);
        nonce = b.getLong();
        timestamp = Instant.ofEpochMilli(b.getLong());
        difficulty = b.getShort();
        noTransactions = 0;
        short numberOfTransactions = b.getShort();
        transactions = new ArrayList<>(numberOfTransactions);
        for (short i = 0; i < numberOfTransactions; i++) {
            byte[] t = new byte[Transaction.TRANSACTION_SIZE];
            b.get(t);
            addTransaction(new Transaction(t));
        }
    }

    public void addTransaction(Transaction t) {
        if (transactions.size() + 1 > Utils.MAX_TRANSACTIONS_PER_BLOCK) {
            throw new IllegalArgumentException(
                    "Block can't have more than "  + Utils.MAX_TRANSACTIONS_PER_BLOCK + " transactions");
        }
        transactions.add(t);
        noTransactions++;
    }

    public boolean validate() {
        byte[] sha256 = sha256(toVerifiableByteArray());
        return checkPrefix(sha256);
    }

    public void mine() {

        if (validate()) {
            return;
        }

        ByteBuffer b = ByteBuffer.wrap(toVerifiableByteArray());

        SecureRandom random = Utils.getSecureRandom();
        long nonce = random.nextLong();

        stop = false;
        long counter = Long.MIN_VALUE;
        while (counter != Long.MAX_VALUE) {
            if (stop) {
                throw new CancellationException("Canceled mining block");
            }

            b.putLong(Utils.NONCE_OFFSET, nonce);
            byte[] sha256 = sha256(b.array());
            if (checkPrefix(sha256)) {
                this.nonce = nonce;
                this.hash = sha256;
                return;
            }

            nonce = (nonce == Long.MAX_VALUE) ? Long.MIN_VALUE : nonce + 1;
            counter++;

        }
        throw new IllegalStateException(
                "No nonce value between " + Long.MIN_VALUE + " and " + Long.MAX_VALUE + " matched for prefix with " + Utils.DIFFICULTY + " leading zeros");
    }

    public void stopMining() {
        stop = true;
    }

    public boolean isPreviousBlock(Block prevBlock) {
        return (getBlockNumber() - prevBlock.getBlockNumber() == 1) && Arrays.equals(getPrevHash(), prevBlock.getHash());
    }

    private byte[] sha256(byte[] b) {
        MessageDigest digest = Utils.getSha256Digest();
        return digest.digest(b);
    }

    private boolean checkPrefix(byte[] sha256) {
        String s = Utils.bytesToHexString(sha256);
        for (int i = 0; i < difficulty; i++) {
            if (s.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private byte[] toVerifiableByteArray() {
        ByteBuffer b = ByteBuffer.allocate(Utils.BLOCKSIZE - 32);
        b.putLong(blockNumber);
        b.put(prevHash);
        b.putLong(nonce);
        b.putLong(timestamp.toEpochMilli());
        b.putShort(difficulty);
        b.putShort(noTransactions);
        if (noTransactions != transactions.size()) {
            throw new IllegalStateException();
        }
        for (Transaction t : transactions) {
            b.put(t.toByteArray());
        }
        return b.array();
    }

    public byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(Utils.BLOCKSIZE);
        b.putLong(blockNumber);
        b.put(prevHash);
        b.put(hash);
        b.putLong(nonce);
        b.putLong(timestamp.toEpochMilli());
        b.putShort(difficulty);
        b.putShort(noTransactions);
        if (noTransactions != transactions.size()) {
            throw new IllegalStateException();
        }
        for (Transaction t : transactions) {
            b.put(t.toByteArray());
        }
        return b.array();
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(byte[] prevHash) {
        this.prevHash = prevHash;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public short getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(short difficulty) {
        this.difficulty = difficulty;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public short getNoTransactions() {
        return noTransactions;
    }

    public void setNoTransactions(short noTransactions) {
        this.noTransactions = noTransactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return getBlockNumber() == block.getBlockNumber() &&
                nonce == block.nonce &&
                difficulty == block.difficulty &&
                noTransactions == block.noTransactions &&
                Arrays.equals(getPrevHash(), block.getPrevHash()) &&
                Arrays.equals(getHash(), block.getHash()) &&
                Objects.equals(timestamp, block.timestamp) &&
                Arrays.equals(transactions.toArray(new Transaction[0]), block.transactions.toArray(new Transaction[0]));
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getBlockNumber(), nonce, timestamp, difficulty, noTransactions, transactions);
        result = 31 * result + Arrays.hashCode(getPrevHash());
        result = 31 * result + Arrays.hashCode(getHash());
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockNumber=" + blockNumber +
                ", prevHash=" + Utils.bytesToHexString(prevHash) +
                ", hash=" + Utils.bytesToHexString(hash) +
                ", nonce=" + nonce +
                ", timestamp=" + timestamp +
                ", difficulty=" + difficulty +
                ", noTransactions=" + noTransactions +
                ", transactions=" + transactions +
                '}';
    }
}
