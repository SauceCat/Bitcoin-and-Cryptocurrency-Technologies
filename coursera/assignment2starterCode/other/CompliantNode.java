import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    double p_graph;
    double p_malicious;
    double p_txDistribution;
    int numRounds;

    boolean[] followees;
    boolean[] malicious;
    Set<Transaction> pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.malicious = new boolean[followees.length];    
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions; 
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> sendTransactions = new HashSet<>(pendingTransactions);
        // clear ahead of next round
        pendingTransactions.clear();
        return sendTransactions; 
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        Set<Integer> senders = candidates.stream().map(candidate -> candidate.sender).collect(toSet());

        // if there is no transaction sent from a followee, then the node is malicious
        for (int i = 0; i < followees.length; i++) {
            if (followees[i] && !senders.contains(i))
                malicious[i] = true;
        }
        // only receive transactions from nodes that are not malicious
        for (Candidate candidate : candidates) { 
            if (!malicious[candidate.sender]) {
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}