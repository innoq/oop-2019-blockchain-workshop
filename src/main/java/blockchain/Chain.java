package blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    /* Begin your code ------------------------------------------------------ */

    public void addBlock(Block block) {
        /*
        Schreibe eine Methode, die einen validen Block an die Chain anhängt.

        Die Kette muss dabei auch valide bleiben und alle Transaktionen im
        neu angehängten Block müssen ausführbar sein.

        Wenn der Block nicht an die Chain angehängt werden kann, werfe eine
        IllegalArgumentException.
         */
        throw new RuntimeException("Not implemented yet");
    }

    /* End your code ------------------------------------------------------ */

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
