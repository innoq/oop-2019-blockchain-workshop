package blockchain;

import java.util.*;

public class TransactionPool {

    private static final Random RANDOM = new Random(Utils.SEED);

    private List<String> accounts = new ArrayList<>();

    public TransactionPool(int noAccounts) {
        for (int i = 0; i < noAccounts; i++) {
            byte[] account = new byte[32];
            while (true) {
                RANDOM.nextBytes(account);
                if (!Arrays.equals(account, Utils.ACCOUNT_ZERO)) {
                    break;
                }
            }
            accounts.add(Utils.bytesToHexString(account));
        }
    }

    public byte[] getRandomAccount() {
        int randomIndex = RANDOM.nextInt(accounts.size());
        return Utils.hexStringToByteArray(accounts.get(randomIndex));
    }

    public Transaction createRewardTransaction(byte[] to) {
        Transaction transaction = new Transaction(Utils.ACCOUNT_ZERO, to, Utils.REWARD);
        return transaction;
    }

    public List<Transaction> createRandomTransactions(Chain chain, int noTransactions) {

        Map<String, Long> accountsBalances = Utils.currentAccountBalances(chain);

        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < noTransactions; i++) {

            String from, to;

            while (true) {
                int randomIndex = RANDOM.nextInt(accountsBalances.size());
                from = (String) accountsBalances.keySet().toArray()[randomIndex];
                if (accountsBalances.get(from) > 0) {
                    break;
                }
            }

            while (true) {
                int randomIndex = RANDOM.nextInt(accounts.size());
                to = accounts.get(randomIndex);
                if (!to.equals(from)) {
                    accountsBalances.putIfAbsent(to, 0L);
                    break;
                }
            }

            long amount;

            while (true) {
                amount = RANDOM.nextInt((accountsBalances.get(from).intValue()+1));
                if (amount > 0) {
                    break;
                }
            }

            long newBalanceFrom = accountsBalances.get(from) - amount;
            long newBalanceTo = accountsBalances.get(to) + amount;
            accountsBalances.put(from, newBalanceFrom);
            accountsBalances.put(to, newBalanceTo);

            Transaction transaction = new Transaction(Utils.hexStringToByteArray(from), Utils.hexStringToByteArray(to), amount);
            transactions.add(transaction);
        }
        return transactions;
    }
}
