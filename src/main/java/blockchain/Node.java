package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node implements Runnable {

    private final Block genesisBlock;
    private final byte[] account;
    private final String name;
    private final TransactionPool transactionPool;
    private final List<Node> peers;

    private final Lock chainLock = new ReentrantLock();
    private transient Chain chain;

    private transient boolean stop = false;
    private transient Block currentBlock;


    public Node(Block genesisBlock, byte[] account, String name, TransactionPool transactionPool) {
        this.account = account;
        peers = new ArrayList<>();
        this.genesisBlock = genesisBlock;
        this.transactionPool = transactionPool;
        this.chain = new Chain(genesisBlock);
        this.name = name;
    }

    public void addPeer(Node node) {
        peers.add(node);
    }

    public void addPeers(Node ...nodes) {
        for (Node node : nodes) {
            addPeer(node);
        }
    }

    public void run() {
        while (!stop) {
            Block block = null;
            Chain newChain = null;
            try {

                block = mineBlock();
                System.out.println(this.getName() + ": found block: " + block);

                if (block.validate()) {
                    boolean blockAdded = false;
                    chainLock.lock();
                    try {
                        chain.addBlock(block);
                        newChain = new Chain(chain.toByteArray());
                        blockAdded = true;
                        stopIfMaxChainLengthReached(newChain);
                    } catch (IllegalArgumentException e) {
                        System.out.println(this.getName() + ": " + e.getMessage());
                    } finally {
                        chainLock.unlock();
                    }

                    if (blockAdded) {
                        broadcastChain(newChain);
                    }
                }
            } catch (CancellationException e) {
                System.out.println(this.getName() + ": " + e.getMessage());
            }

        }
    }

    private Block mineBlock() {
        Block block = chain.prepareNextBlock();
        block.addTransaction(transactionPool.createRewardTransaction(account));
        List<Transaction> randomTransactions = transactionPool.createRandomTransactions(chain, Utils.MAX_TRANSACTIONS_PER_BLOCK - 1);
        randomTransactions.forEach(tx -> block.addTransaction(tx));
        currentBlock = block;
        block.mine();
        return block;
    }

    public void broadcastChain(Chain chain) {
        peers.forEach(peer -> {
            System.out.println(this.getName() + ": send chain to " + peer.getName());
            peer.receiveChain(chain.toByteArray());
        });
    }

    public boolean receiveChain(byte[] chainBlob) {

        Chain receivedChain = null;
        try {
            receivedChain = new Chain(chainBlob);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
        System.out.println(this.getName() + ": received new chain " + receivedChain);
        System.out.println(this.getName() + ": received chain length: " + receivedChain.length());

        try {
            chainLock.lock();
            if (receivedChain.length() <= chain.length()) {
                System.out.println(this.getName() + ": discard chain. Received is not longer than own chain.");
                return false;
            } else if (!receivedChain.getGenesisBlock().equals(genesisBlock)) {
                System.out.println(this.getName() + ": discard chain. Received chain has other genesis block.");
                return false;
            }
            stopIfMaxChainLengthReached(receivedChain);
            chain = receivedChain;
            stopMining();
            System.out.println(this.getName() + ": swapped chain");
        } finally {
            chainLock.unlock();
        }

        return true;
    }

    private void stopIfMaxChainLengthReached(Chain chain) {
        if (chain.length() >= Utils.MAX_CHAIN_LENGTH) {
            System.out.println(this.getName() + ": new chain has maximum length.");
            stop();
        }
    }

    public void stop() {
        if (stop) {
            System.out.println(this.getName() + ": already stopped");
        } else {
            System.out.println(this.getName() + ": stop mining");
            stop = true;
            stopMining();
        }
    }

    private void stopMining() {
        if (currentBlock != null) {
            currentBlock.stopMining();
        }
    }

    public Block getGenesisBlock() {
        return genesisBlock;
    }

    public byte[] getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }
    
    public List<Node> getPeers() {
        return peers;
    }
    
    public Chain getChain() {
        return chain;
    }

    @Override
    public String toString() {
        return "Node{" +
                "genesisBlock=" + genesisBlock +
                ", account=" + Utils.bytesToHexString(account) +
                ", name='" + name + '\'' +
                ", chain=" + chain +
                '}';
    }
}
