import java.security.PublicKey;
import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    // the unspent outputs
    private UTXOPool uPool;

    public TxHandler(UTXOPool utxoPool) {
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
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        ArrayList<Transaction.Input> inputs = tx.getInputs();

        // avoid double spend
        UTXOPool inputPool = new UTXOPool();

        // if there is no inputs or outputs
        if (inputs == null || outputs == null) {
            return false;
        }
        
        for (int i = 0; i < inputs.size(); i++) {
            Transaction.Input in = inputs.get(i);
            UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);

            /* all outputs claimed by {@code tx} are in the current UTXO pool */
            if (!uPool.contains(ut)) {
                return false;
            }

            /* no UTXO is claimed multiple times by {@code tx} */
            if (inputPool.contains(ut)) {
                return false;
            }

            /* the signatures on each input of {@code tx} are valid */
            // get the corresponding publicKey
            Transaction.Output ot = uPool.getTxOutput(ut);
            PublicKey publicKey = ot.address;

            // get signature
            byte[] signature = in.signature;

            if(!Crypto.verifySignature(publicKey, tx.getRawDataToSign(i), signature)) {
                return false;
            }

            inputPool.addUTXO(ut, ot);
        }

        /* all of {@code tx}s output values are non-negative */
        for (Transaction.Output ot : outputs) {
            if (ot.value < 0) {
                return false;
            }
        }

        /* the sum of {@code tx}s input values is greater than or equal to the sum of its output values */
        Double inputSum = 0.0;
        Double outputSum = 0.0;

        for (Transaction.Input in : inputs) {
            UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output ot = uPool.getTxOutput(ut);
            inputSum = inputSum + ot.value;
        }

        for (Transaction.Output ot : outputs) {
            outputSum = outputSum + ot.value;
        }

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
        ArrayList<Transaction> validTx = new ArrayList<Transaction>();
        int prevTxLen;
        int newTxLen;

        do {
            prevTxLen = validTx.size();
            for (int i = 0; i < possibleTxs.length; i++) {
                Transaction tx = possibleTxs[i];
                if (validTx.contains(tx)) {
                    continue;
                }
                if (isValidTx(tx)) {
                    validTx.add(tx);

                    // update the UTXOPool
                    ArrayList<Transaction.Input> inputs = tx.getInputs();
                    ArrayList<Transaction.Output> outputs = tx.getOutputs();

                    // remove all claimed outputs from UTXOPool
                    for (Transaction.Input in : inputs) {
                        UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
                        uPool.removeUTXO(ut);
                    }

                    // add all outputs into UTXOPool
                    for (int j = 0; j < outputs.size(); j++) {
                        UTXO ut = new UTXO(tx.getHash(), j);
                        uPool.addUTXO(ut, outputs.get(j));
                    }
                }
            }
            newTxLen = validTx.size();
        } while (prevTxLen != newTxLen);

        Transaction[] validTxOut = new Transaction[validTx.size()];
        int i = 0;
        for (Transaction tx : validTx) {
            validTxOut[i++] = tx;
        }

        return validTxOut;
    }

}