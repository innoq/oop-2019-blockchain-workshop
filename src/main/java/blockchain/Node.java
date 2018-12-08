package blockchain;

import java.util.ArrayList;
import java.util.List;
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

    /* Begin your code ------------------------------------------------------ */

    public void run() {

        /*
        Schreibe eine Methode die solange neue Blöcke mined bis die Länge
        der Chain Utils.MAX_CHAIN_LENGTH ist oder stop true ist.
         */
        throw new RuntimeException("Not implemented yet");
    }

    public void broadcastChain() {

        /*
        Schreibe eine Methode die die Chain des Nodes an alle peer nodes
        verteilt sobald ein neuer Block gefunden wurde.

        TIPP: Zum Verteilen rufe die Methode receiveChain auf den
        peer nodes auf.
         */
        throw new RuntimeException("Not implemented yet");
    }

    public boolean receiveChain(byte[] chainBlob) {

        /*
        Erweitere die Methode so, dass das Mining gestoppt wird,
        wenn Länge der getauschet Chain Utils.MAX_CHAIN_LENGTH ist.
         */

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
            chain = receivedChain;
            stopMining();
            System.out.println(this.getName() + ": swapped chain");
        } finally {
            chainLock.unlock();
        }

        return true;
    }

    /* End your code ------------------------------------------------------ */

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
