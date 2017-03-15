package bitcoin03;

import java.security.PublicKey;
import java.util.ArrayList;

public class TxHandler {
    /** current UTXOPool for the ledger **/
    public UTXOPool uPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        uPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double inputSum = 0;
        double outputSum = 0;
        UTXOPool inputPool = new UTXOPool();
        
        int i = 0;
        for (Transaction.Input in : tx.getInputs()) {
        	
        	/** all outputs claimed by {@code tx} are in the current UTXO pool **/
            /** no UTXO is claimed multiple times by {@code tx} **/
        	UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
        	if (!uPool.contains(utxo)) {
        		return false;
        	}
        	if (inputPool.contains(utxo)) {
        		return false;
        	}
        	
        	Transaction.Output output = uPool.getTxOutput(utxo);
        	PublicKey pubKey = output.address;
        	
        	/** the signatures on each input of {@code tx} are valid **/
        	if (!Crypto.verifySignature(pubKey, tx.getRawDataToSign(i), in.signature)) {
        		return false;
        	}
        	
        	inputPool.addUTXO(utxo, output);
        	inputSum += output.value;
        	i++;
        }
        
        for (Transaction.Output op : tx.getOutputs()) {
            
            /** all of {@code tx}s output values are non-negative **/
            if (op.value < 0) {
                return false;
            }
            
            outputSum += op.value;
        }
        
        /** the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise **/
        if (inputSum < outputSum) {
            return false;
        }
        
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> finalTran = new ArrayList<Transaction>();
        boolean foundtx = false;
        
        do {
        	foundtx = false;
        	for (Transaction tran : possibleTxs) {
        		if (finalTran.contains(tran)) {
        			continue;
        		}
                if (isValidTx(tran)) {
                	foundtx=true;
                    finalTran.add(tran);
                    
                    for (Transaction.Input in : tran.getInputs()) {
                        UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                        uPool.removeUTXO(utxo);
                    }
                    
                    for (int i = 0; i < tran.getOutputs().size(); i++) {
                    	UTXO utxo = new UTXO(tran.getHash(), i);
                    	uPool.addUTXO(utxo, tran.getOutput(i));
                    }
                }
            }
        } while (foundtx);
 
        Transaction[] outTran = new Transaction[finalTran.size()];
        int i = 0;
        for(Transaction tran : finalTran) {
            outTran[i] = tran;
            i++;
        }
        return outTran;
    }
    
    public UTXOPool getUTXOPool() {
    	return uPool;
    }
}

