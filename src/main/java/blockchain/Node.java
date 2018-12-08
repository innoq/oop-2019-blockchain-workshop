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

    public void run() {

    }

    /* Begin your code ------------------------------------------------------ */

    public boolean receiveChain(byte[] chainBlob) {
        /*
        Schreibe eine Methode die eine Chain von einem anderen Knoten entgegen
        nimmt.

        Sollte diese valide und länger sein als die aktuelle Chain des Knotens
        tausche die aktuelle Chain gegen die des anderen Kontens.

        Wenn die Chain ausgetauscht wurde gebe true zurück, ansonsten false.
         */
        throw new RuntimeException("Not implemented yet");
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
