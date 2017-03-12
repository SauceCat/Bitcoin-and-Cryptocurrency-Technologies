import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private boolean[] followees;
    // list for storing malicious followees
    private boolean[] malicious;
    private Set<Transaction> pendingTransactions;
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private HashMap<Transaction, Set<Integer>> uniqueTx = new HashMap<>();
    private int currentRound;
    private int folsLen = 0;
    private int malsLen = 0;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.malicious = new boolean[followees.length]; 
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) {
                folsLen++;
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> sendTransactions = new HashSet<>(pendingTransactions);
        // clear the pendingTransactions for the next round
        pendingTransactions.clear();

        if (currentRound == numRounds) {
            Set<Transaction> outTx = new HashSet<>();
            for (Transaction tx : sendTransactions) {
                int count = uniqueTx.get(tx).size();
                if (count >= Math.min(folsLen - malsLen, (folsLen - malsLen) * p_graph * p_txDistribution * numRounds)) {
                    outTx.add(tx);
                }
            }
            return outTx;
        }
        return sendTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        currentRound++;
        Set<Integer> senders = candidates.stream().map(candidate -> candidate.sender).collect(toSet());

        // if there is no transaction sent from a node
        // then the node should be a malicious node
        for (int i = 0; i < followees.length; i++) {
            if (followees[i] && !senders.contains(i)) {
                malicious[i] = true;
            }
        }

        // only receive transaction from nodes that are compliant
        for (Candidate candidate : candidates) {
            if (!malicious[candidate.sender]) {
                pendingTransactions.add(candidate.tx);

                if (!uniqueTx.containsKey(candidate.tx)) {
                    Set<Integer> fols = new HashSet<>();
                    uniqueTx.put(candidate.tx, fols);
                }

                // update the unique sender list for a transaction
                uniqueTx.get(candidate.tx).add(candidate.sender);
            }
        }

        // update length of malicious nodes
        for (int i = 0; i < malicious.length; i++) {
            if (malicious[i]) {
                malsLen++;
            }
        }
    }
}
