package blockchain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class NodeMining {

    private Node node;

    @Before
    public void setup() {
        TransactionPool transactionPool = new TransactionPool(20);

        byte[] firstRewardAccount = transactionPool.getRandomAccount();
        Transaction rewardTransaction = transactionPool.createRewardTransaction(firstRewardAccount);
        Block genesisBlock = new Block(0, new byte[32], Utils.DIFFICULTY, rewardTransaction);
        genesisBlock.mine();

        byte[] miningAccount = transactionPool.getRandomAccount();
        node = new Node(genesisBlock, miningAccount, "Test node", transactionPool);

    }

    @Test
    public void shouldStopMining() throws InterruptedException {
        Thread t = new Thread(node);
        t.start();
        node.stop();
        t.join();
        Assert.assertTrue(node.getChain().length() < Utils.MAX_CHAIN_LENGTH);
    }

    @Test
    public void shouldMineMaxChainLengthBlocks() throws InterruptedException {
        Thread t = new Thread(node);
        t.start();
        t.join();
        Assert.assertTrue(node.getChain().length() == Utils.MAX_CHAIN_LENGTH);
    }

    @Test
    public void shouldBroadcastChain() throws InterruptedException {
        Node peer = Mockito.mock(Node.class);
        node.addPeer(peer);
        Thread t = new Thread(node);
        t.start();
        t.join();
        Mockito.verify(peer, Mockito.atLeastOnce()).receiveChain(Mockito.any(byte[].class));
    }
}
