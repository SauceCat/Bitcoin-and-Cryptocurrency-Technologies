import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private boolean[] followees;
    private Set<Transaction> pendingTransactions;
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private HashMap<Transaction, Set<Integer>> uniqueTx = new HashMap<>();
    private int currentRound;
    private int folsLen = 0;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) {
                this.folsLen++;
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        if (currentRound == numRounds) {
            Set<Transaction> outTx = new HashSet<>();
            for (Transaction tx : pendingTransactions) {
                int count = uniqueTx.get(tx).size();
                //if (count >= Math.min(folsLen * (1 - p_malicious), folsLen * (1 - p_malicious) * p_graph * p_txDistribution * numRounds)) {
                if (count >= 1) {
                    outTx.add(tx);
                }
            }
            return outTx;
        }
        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        currentRound++;
        for (Candidate candidate : candidates) {
            if (!this.pendingTransactions.contains(candidate.tx)) {
                this.pendingTransactions.add(candidate.tx);
            }
            if (!uniqueTx.containsKey(candidate.tx)) {
                Set<Integer> fols = new HashSet<>();
                uniqueTx.put(candidate.tx, fols);
            }
            uniqueTx.get(candidate.tx).add(candidate.sender);
        }
    }
}
