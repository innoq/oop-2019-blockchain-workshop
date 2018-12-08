package blockchain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockchainNetwork {

    public static final int NO_ACCOUNTS = 100;

    public static final int NO_NODES = 20;

    public static void main(String[] args) {

        TransactionPool transactionPool = new TransactionPool(NO_ACCOUNTS);

        byte[] firstRewardAccount = transactionPool.getRandomAccount();
        Transaction rewardTransaction = transactionPool.createRewardTransaction(firstRewardAccount);
        Block genesisBlock = new Block(0, new byte[32], Utils.DIFFICULTY, rewardTransaction);
        genesisBlock.mine();

        List<Node> nodes = new ArrayList<>();
        for (int i = 1; i <= NO_NODES; i++) {
            byte[] miningAccount = transactionPool.getRandomAccount();
            Node node = new Node(genesisBlock, miningAccount, "Node " + i, transactionPool);
            nodes.add(node);
        }

        nodes.forEach(currentNode -> {
            nodes.forEach(otherNodes -> {
                if (!currentNode.equals(otherNodes)) {
                    currentNode.addPeer(otherNodes);
                }
            });
        });

        Set<Thread> threads = new HashSet<>();
        nodes.forEach(node -> {
            Thread thread = new Thread(node);
            threads.add(thread);
            thread.start();
        });

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        });

        nodes.forEach(n -> System.out.println(n.getChain()));

        int randomIndex = Utils.getSecureRandom().nextInt(nodes.size());
        Node randomNode = nodes.get(randomIndex);
        Chain chain = randomNode.getChain();
        Utils.printAccountBalances(chain);

    }
}