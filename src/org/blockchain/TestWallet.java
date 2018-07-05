package org.blockchain;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.TransactionUtils;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CipherException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.walletserver.WalletClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TestWallet {
    private static final Logger logger = LoggerFactory.getLogger("TestClient");

    /**
     * Print information of block in terminal.
     * @param blockNum  a number giving height of this block
     */
    public static void getBlock(long blockNum) throws InvalidProtocolBufferException {
        Block block = TronUtils.getBlock(blockNum);
        if (block == null) {
            logger.info("No block for num : " + blockNum);
            return;
        }
        //logger.info(Utils.printBlock(block));
        List<Transaction> listTransactions = block.getTransactionsList();

        for(int i = 0; i < listTransactions.size(); i++) {
            Transaction transaction = listTransactions.get(i);
            List<Transaction.Contract> listContract = transaction.getRawData().getContractList();
            for(int j = 0; j < listContract.size(); ++j) {
                Transaction.Contract contract = listContract.get(j);
                Transaction.Contract.ContractType contractType = contract.getType();
                System.out.println(contractType.toString());
                System.out.println("Transaction " + i + ":");
                System.out.println("  txid: " + TronUtils.getTransactionId(transaction));
                System.out.println("  hash:" + TronUtils.getTransactionHash(transaction));

                if (contractType == Transaction.Contract.ContractType.TransferContract) {
                    Contract.TransferContract transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
                    System.out.println("  From: " + TronUtils.getTransactionOwner(transferContract));
                    System.out.println("  To: " + TronUtils.getTransactionToAddress(transferContract));
                    System.out.println("  Amount: " + transferContract.getAmount());
                    System.out.println();
                }
                if (contractType == Transaction.Contract.ContractType.TransferAssetContract) {
                    Contract.TransferAssetContract transferAssetContract = contract.getParameter().unpack(Contract.TransferAssetContract.class);
                    System.out.println("  From: " + TronUtils.getTransactionOwner(transferAssetContract));
                    System.out.println("  To: " + TronUtils.getTransactionToAddress(transferAssetContract));
                    System.out.println("  Amount: " + transferAssetContract.getAmount());
                    System.out.println();
                }
            }
        }
    }

    /**
     * Print information of transaction in terminal.
     * @param transactionId  a string giving string hash of transaction
     */
    public static void getTransactionById(String transactionId) {
        Optional<Transaction> result = TronUtils.getTransactionById(transactionId);
        if (result.isPresent()) {
            Transaction transaction = result.get();
            logger.info(Utils.printTransaction(transaction));
        } else {
            logger.info("getTransactionById " + " failed !!");
        }
    }


    public static void registerWallet(String password) throws CipherException, IOException {
        String fileName = TronUtils.registerWallet(password);

        if (null == fileName) {
            logger.info("Register wallet failed !!");
            return;
        }
        logger.info("Register a wallet successful, keystore file name is " + fileName);
    }

    public static void getBlockByLimitNext(long startBlockNum, long endBlockNum) {
        Optional<BlockList> result = TronUtils.getBlockByLimitNext(startBlockNum, endBlockNum);
        if (result.isPresent()) {
            BlockList blockList = result.get();
            logger.info(Utils.printBlockList(blockList));
        } else {
            logger.info("GetBlockByLimitNext " + " failed !!");
        }
    }

    public static void getBlockByLatestNum(long num) {
        Optional<BlockList> result = TronUtils.getBlockByLatestNum(num);
        if (result.isPresent()) {
            GrpcAPI.BlockList blockList = result.get();
            logger.info(Utils.printBlockList(blockList));
        } else {
            logger.info("getBlockByLatestNum " + " failed !!");
        }
    }

    public static void getAccount(String accountAddress) {
        Account account = TronUtils.getAccount(accountAddress);
        if (account == null) {
            logger.info("GetAccount failed !!!!");
        } else {
            logger.info("\n" + Utils.printAccount(account));
        }
    }

    public static void sendCoin(String fromAddress, String password, String walletFilePath, String toAddress, long amount) {
        boolean result = TronUtils.sendCoinFromFileAndPassword(fromAddress, password, walletFilePath, toAddress, amount);
        if (result) {
            logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
        }
    }


    public static void main(String[] args) throws InvalidProtocolBufferException {
        getBlock(	73308);
        //getTransactionById("103e376d01ea205a8e3ba6ad36f55322485412565b3192d088044de21f8ce837");

        /*
        try {
            registerWallet("tronUTS123");
        } catch (Exception e) {
            System.out.println(e);
        }
        */

        //getBlockByLimitNext(73308, 73310);
        //getBlockByLatestNum(2);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");

        //String password = "tronUTS123";
        //String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";
        //sendCoin("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", 1000000);
    }
}
