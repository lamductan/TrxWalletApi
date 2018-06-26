import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.common.utils.Utils;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.Optional;

public class trxWallet {
    private static final Logger logger = LoggerFactory.getLogger("TestClient");
    private Client client = new Client();


    /**
     * Print information of block in terminal.
     * @param blockNum  a number giving height of this block
     */
    public void GetBlock(long blockNum) {
        Protocol.Block block = client.getBlock(blockNum);
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
    public void GetTransactionById(String transactionId) {
        Optional<Protocol.Transaction> result = WalletClient.getTransactionById(transactionId);
        if (result.isPresent()) {
            Protocol.Transaction transaction = result.get();
            logger.info(Utils.printTransaction(transaction));
        } else {
            logger.info("getTransactionById " + " failed !!");
        }
    }

    /**
     * Print information of transaction in terminal.
     * @param transactionId  a string giving string hash of transaction
     * There is a unknown bug when calling this api.
     */
    public void GetTransactionInfoById(String transactionId) {
        Optional<Protocol.TransactionInfo> result = WalletClient.getTransactionInfoById(transactionId);
        if (result.isPresent()) {
            Protocol.TransactionInfo transactionInfo = result.get();
            logger.info(Utils.printTransactionInfo(transactionInfo));
        } else {
            logger.info("getTransactionInfoById " + " failed !!");
        }
    }

    /**
     * Print information of all blocks in range(startBlockNum, endBlockNum) inclusive.
     * @param startBlockNum  a number giving height of start block
     * @param endBlockNum  a number giving height of end block
     */
    public void GetBlockByLimitNext(long startBlockNum, long endBlockNum) {
        Optional<GrpcAPI.BlockList> result = WalletClient.getBlockByLimitNext(startBlockNum, endBlockNum);
        if (result.isPresent()) {
            GrpcAPI.BlockList blockList = result.get();
            logger.info(Utils.printBlockList(blockList));
        } else {
            logger.info("GetBlockByLimitNext " + " failed !!");
        }
    }

    /**
     * Print information of all latest num blocks.
     * @param num  a number giving number of latest blocks
     */
    public void GetBlockByLatestNum(long num) {
        Optional<GrpcAPI.BlockList> result = WalletClient.getBlockByLatestNum(num);
        if (result.isPresent()) {
            GrpcAPI.BlockList blockList = result.get();
            logger.info(Utils.printBlockList(blockList));
        } else {
            logger.info("getBlockByLatestNum " + " failed !!");
        }
    }

    /**
     * Print information of account.
     * @param accountAddress  a string giving address of account
     */
    public void GetAccount(String accountAddress) {
        byte[] addressBytes = WalletClient.decodeFromBase58Check(accountAddress);
        Protocol.Account account = WalletClient.queryAccount(addressBytes);
        if (account == null) {
            logger.info("GetAccount failed !!!!");
        } else {
            logger.info("\n" + Utils.printAccount(account));
        }
    }

    public static void main(String[] args) {
        trxWallet wallet = new trxWallet();
        //wallet.GetBlock(58750);
        //wallet.GetTransactionById("f35350480a219cb8f0eaccde8b4a32ab7d45a6255ec34d515df691c40cda6790");
        //wallet.GetBlockByLimitNext(78800, 78802);
        //wallet.GetBlockByLatestNum(3);
        wallet.GetAccount("TTEx8ctJgEk6jpgjX9jG2CBZVmMTJTsvzq");
    }
}
