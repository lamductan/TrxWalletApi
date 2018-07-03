import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.utils.Utils;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;

import java.util.Optional;

public class TestWallet {
    private static final Logger logger = LoggerFactory.getLogger("TestClient");
    private TrxWallet trxwallet = new TrxWallet();

    /**
     * Print information of block in terminal.
     * @param blockNum  a number giving height of this block
     */
    public static void getBlock(long blockNum) {
        Block block = TrxWallet.getBlock(blockNum);
        if (block == null) {
            logger.info("No block for num : " + blockNum);
            return;
        }
        logger.info(Utils.printBlock(block));
    }

    /**
     * Print information of transaction in terminal.
     * @param transactionId  a string giving string hash of transaction
     */
    public static void getTransactionById(String transactionId) {
        Optional<Transaction> result = TrxWallet.getTransactionById(transactionId);
        if (result.isPresent()) {
            Protocol.Transaction transaction = result.get();
            logger.info(Utils.printTransaction(transaction));
        } else {
            logger.info("getTransactionById " + " failed !!");
        }
    }


    public static void main(String[] args) {
        getBlock(	73308);
        getTransactionById("103e376d01ea205a8e3ba6ad36f55322485412565b3192d088044de21f8ce837");

    }
}
