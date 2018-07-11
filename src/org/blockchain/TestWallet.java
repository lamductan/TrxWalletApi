package org.blockchain;

import com.google.protobuf.ByteString;
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
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.walletserver.WalletClient;
import org.tron.protos.Protocol.Transactions;
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
            //System.out.println(transaction.getRawData().getRefBlockHash().toByteArray().toString());
            System.out.println("Transaction " + i + ":");

            //System.out.println(transaction.getRawData().toString());
            System.out.println(TronUtils.getTransactionAmount(transaction));
            System.out.println("  txid: " + TronUtils.getTransactionId(transaction));
            System.out.println("  hash:" + TronUtils.getTransactionHash(transaction));
            for(int j = 0; j < listContract.size(); j++) {
                Transaction.Contract contract = listContract.get(j);
                Transaction.Contract.ContractType contractType = contract.getType();
                System.out.println("    Contract " + j + ":");
                System.out.println("      Contract type: " + contractType.toString());
                System.out.println("      From: " + TronUtils.getContractOwner(contract));
                System.out.println("      To: " + TronUtils.getContractToAddress(contract));
                System.out.println("      Amount: " + TronUtils.getContractAmount(contract));
                if (contractType.toString().equals("TransferAssetContract")) {
                     Contract.TransferAssetContract transferAssetContract = contract.getParameter().unpack(Contract.TransferAssetContract.class);
                     System.out.println("      Issue name: " + TronUtils.getAssetContractName(transferAssetContract));
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

    public static Transaction sendCoinAndGetTrxId(String fromAddress, String password, String walletFilePath, String toAddress, long amount) {
        Transaction transaction = TronUtils.sendCoinFromFileAndPasswordAndGetTrxId(fromAddress, password, walletFilePath, toAddress, amount);
        boolean result = (transaction==null)?false:true;
        if (result) {
            logger.info("Send " + amount + " " + "TRX" + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + "TRX" + " drop to " + toAddress + " failed !!");
        }
        return transaction;
    }

    public static void sendToken(String fromAddress, String password, String walletFilePath, String toAddress, String tokenName, long amount) {
        boolean result = TronUtils.sendTokenFromFileAndPassword(fromAddress, password, walletFilePath, toAddress, tokenName, amount);
        if (result) {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " failed !!");
        }
    }

    public static Transaction sendTokenAndGetTrxId(String fromAddress, String password, String walletFilePath, String toAddress, String tokenName, long amount) {
        Transaction transaction = TronUtils.sendTokenFromFileAndPasswordAndGetTrxId(fromAddress, password, walletFilePath, toAddress, tokenName, amount);
        boolean result = (transaction==null)?false:true;
        if (result) {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " failed !!");
        }
        return transaction;
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
//        getBlock(33039);
        //getTransactionById("103e376d01ea205a8e3ba6ad36f55322485412565b3192d088044de21f8ce837");
        getTransactionById("d7ae33c537e2cba651076571913b9f63a0c4fb96379a98da2e0b60336867b38e");


//        try {
//            registerWallet("Trong-DatPhan0411");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        System.out.print(TronUtils.backUpWallet("Trong-DatPhan0411","UTC--2018-07-10T07-51-29.623000000Z--TMoki8ACYc6GUKm8Wo2TZwwfkRCWgqe6Tq.json"));

        //getBlockByLimitNext(73308, 73310);
        //getBlockByLatestNum(2);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");

        String password = "tronUTS123";
        String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";
//        Transaction transaction = sendTokenAndGetTrxId("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", "DucTan",5);
//        System.out.print(TronUtils.getTransactionId(transaction));

//        Transaction transaction = sendCoinAndGetTrxId("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ",5);
//        System.out.print(TronUtils.getTransactionId(transaction));

        //sendCoin("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", 1000000);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");
        //getAccount("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ");
        //sendToken("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", "DucTan", 1);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");
        //getAccount("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ");
        // git ignore
    }
}
