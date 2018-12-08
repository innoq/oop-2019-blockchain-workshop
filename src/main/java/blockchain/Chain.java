package blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chain {

    private List<Block> blocks;

    public Chain(Block genesisBlock) {
        blocks = new ArrayList<>();
        addBlock(genesisBlock);
    }

    public Chain(byte[] chain) {
        if (chain.length % Utils.BLOCKSIZE != 0) {
            throw new IllegalArgumentException("Illegal chain size");
        }
        blocks =  new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(chain);
        byte[] blockBytes = new byte[Utils.BLOCKSIZE];
        for (int i = chain.length / Utils.BLOCKSIZE; i > 0; i--) {
            buffer.get(blockBytes);
            Block block = new Block(blockBytes);
            addBlock(block);
        }
    }

    public void addBlock(Block block) {

        if (!blocks.isEmpty()) {
            Block lastBlock = getLastBlock();
            if (!block.isPreviousBlock(lastBlock)) {
                throw new IllegalArgumentException("Can't add block: Not next block.");
            }
        }

        short difficulty = block.getDifficulty();
        if (difficulty != Utils.DIFFICULTY) {
            throw new IllegalArgumentException("Can't add block: Difficulty is: " + difficulty + " but should be: " + Utils.DIFFICULTY + ".");
        }

        if (!block.validate()) {
            throw new IllegalArgumentException("Can't add block: Verification failed.");
        }

        if (!transactionsCanBeExecuted(block)) {
            throw new IllegalArgumentException("Can't add block: contains invalid transaction");
        }

        blocks.add(block);
    }


    private boolean transactionsCanBeExecuted(Block block) {

        Map<String, Long> accountBalances = Utils.currentAccountBalances(this);

        boolean rewardTransactionExecuted = false;
        for (Transaction t : block.getTransactions()) {

            long amount = t.getAmount();
            if (amount <= 0) {
                return false;
            }

            String from = t.getFromAsString();
            String to = t.getToAsString();

            if (Utils.ACCOUNT_ZERO_AS_STRING.equals(from)) {
                if (rewardTransactionExecuted) {
                    return false;
                }
                if (amount != Utils.REWARD) {
                    return false;
                }
                rewardTransactionExecuted = true;
            } else {
                long balanceFrom = accountBalances.get(from);
                if (balanceFrom < amount) {
                    return false;
                }
                accountBalances.put(from, balanceFrom - amount);
            }
            accountBalances.putIfAbsent(to, 0L);
            long balanceTo = accountBalances.get(to);
            accountBalances.put(to, balanceTo + amount);
        }
        return true;
    }

    public Block prepareNextBlock() {

        Block lastBlock = getLastBlock();
        long blockNumber = lastBlock.getBlockNumber() + 1;
        byte[] lastBlockHash = lastBlock.getHash();

        Block newBlock = new Block(blockNumber, lastBlockHash, Utils.DIFFICULTY);
        return newBlock;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (Block block : blocks) {
            try {
                b.write(block.toByteArray());
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }
        return b.toByteArray();
    }

    public Block getGenesisBlock() {
        return blocks.get(0);
    }

    private Block getLastBlock() {
        return blocks.get(blocks.size()-1);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public int length() {
        return blocks.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chain{");
        for (Block block : blocks) {
            sb.append(block);
            sb.append(" ");
        }
        sb.append("}");
        return sb.toString();
    }
}
