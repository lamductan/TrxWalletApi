package org.blockchain;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CipherException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.api.GrpcAPI.BlockList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
        logger.info(Utils.printBlock(block));

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

    public static void getBlockCount() {
        System.out.println("Block height: " + TronUtils.getBlockCount());
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
    public static void registerAccount(String password) throws CipherException, IOException {
        String privateKey = TronUtils.registerAccount(password);

        if (null == privateKey) {
            logger.info("Register account failed !!");
            return;
        }
        logger.info("Register account successful, private key is " + privateKey);
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
        boolean result = (transaction == null) ? false : true;
        if (result) {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " failed !!");
        }
        return transaction;
    }

    public static void sendCoin(String privateKey, String toAddress, long amount) {
        boolean result = TronUtils.sendCoin(privateKey, toAddress, amount);
        if (result) {
            logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
        }
    }

    public static Transaction sendCoinAndGetTrxId(String privateKey, String toAddress, long amount) {
        Transaction transaction = TronUtils.sendCoinAndGetTrxId(privateKey, toAddress, amount);
        boolean result = (transaction==null)?false:true;
        if (result) {
            logger.info("Send " + amount + " " + "TRX" + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + "TRX" + " drop to " + toAddress + " failed !!");
        }
        return transaction;
    }

    public static void sendToken(String privateKey, String toAddress,String tokenName, long amount) {
        boolean result = TronUtils.sendToken(privateKey, toAddress, tokenName, amount);
        if (result) {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " failed !!");
        }
    }

    public static Transaction sendTokenAndGetTrxId(String privateKey, String toAddress,String tokenName, long amount) {
        Transaction transaction = TronUtils.sendTokenAndGetTrxId(privateKey, toAddress, tokenName, amount);
        boolean result = (transaction == null) ? false : true;
        if (result) {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " " + tokenName + " drop to " + toAddress + " failed !!");
        }
        return transaction;
    }

    public static void backupWallet(String password, String walletFilePath) {
        System.out.println(TronUtils.backUpWallet(password, walletFilePath));
    }

    public static void findMultiContractTransaction(long startBlock, long endBlock) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("MultipleContractTransaction.txt", "UTF-8");
        for(long i = startBlock; i < endBlock; i++) {
            System.out.println("Block " + i);
            Block block = TronUtils.getBlock(i);
            List<Transaction> listTransactions = block.getTransactionsList();

            for (int j = 0; j < listTransactions.size(); j++) {
                Transaction transaction = listTransactions.get(j);
                List<Transaction.Contract> listContract = transaction.getRawData().getContractList();
                if (listContract.size() > 1) {
                    System.out.println("Block " + i + " - Transaction " + j);
                    writer.println("Block " + i + " - Transaction " + j);
                }
            }
        }
        writer.close();
    }

    public static void statictisBlocks() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("NumberTransactionInEachBlock.csv", "UTF-8");
        writer.println("Block,#Transaction,#Contract,#Ret");
        for(long i = 0 ; i < TronUtils.getBlockCount(); i++) {
            Block block = TronUtils.getBlock(i);
            int transactionsCount = block.getTransactionsCount();
            if(transactionsCount > 0) {
                List<Transaction> listTransactions = block.getTransactionsList();
                long contractCountInBlock = 0 ;
                long totalTransactionRetCountInBlock = 0;
                for(int j = 0; j < transactionsCount; j++) {
                    Transaction transaction = listTransactions.get(j);
                    contractCountInBlock += transaction.getRawData().getContractCount();
                    totalTransactionRetCountInBlock += transaction.getRetCount();
                }
                System.out.println(i + ","
                                     + transactionsCount + ","
                                     + contractCountInBlock + ","
                                     + totalTransactionRetCountInBlock);
                writer.println(i + ","
                               + transactionsCount + ","
                               + contractCountInBlock + ","
                               + totalTransactionRetCountInBlock);
            }
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException, CipherException {
        String privateKey = "dbb5043d470012cac45d53852a41aa76bd8010fe66df325358b7608c513ff240"; // TK... account
        String add = "TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An";
        String privateKey1 = "beb49375aaad23422584b1a2b4c755c53a5beb4356f4a7c1b8a2f28ce7162c96";
        String add1 = "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ";
        String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";

        String password = "tronUTS123";

        System.out.print(TronUtils.sendAllToken(privateKey1,"TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", "LDTan"));

        //registerAccount(password);
        //System.out.print(TronUtils.getAddressFromPrivKey("dbb5043d470012cac45d53852a41aa76bd8010fe66df325358b7608c513ff240"));
        //Transaction transaction = sendCoinAndGetTrxId("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An",password, walletFilePath,"TLj47nYMtSsFBXKyX66NMBiFYic39pLqSy",10);
        //getBlock(-1);
       // getTransactionById("103e376d01ea205a8e3ba6ad36f55322485412565b3192d088044de21f8ce837");
        //getTransactionById("f230181c2636a17003098521bb631c0beb5c041fce66636efeafd0992a1af270");
        //getTransactionById("535b5b209cff86fc997bf931b17d48918fa2860991cf98d5067dbc2ffc441ff4");


//        try {
//            registerWallet("Trong-DatPhan0411");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        System.out.print(TronUtils.backUpWallet("Trong-DatPhan0411","UTC--2018-07-10T07-51-29.623000000Z--TMoki8ACYc6GUKm8Wo2TZwwfkRCWgqe6Tq.json"));

        //getBlockByLimitNext(73308, 73310);
        //getBlockByLatestNum(2);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");

//        String password = "tronUTS123";
//        String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";

//

        //Transaction transaction = sendCoinAndGetTrxId("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ",5);



        // System.out.print(TronUtils.getTransactionId(transaction));




//        String password1 = "lamductan@123";
//        String walletFilePath1 = "UTC--2018-06-29T06-53-22.686000000Z--TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ.json";
//        for(int i = 0; i < 1; ++i) {
//            Transaction transaction = sendCoinAndGetTrxId("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", password1, walletFilePath1, "TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", 500000000L);
//            System.out.print(transaction.toByteArray().length);
//        }
            //for(int i = 0; i < 20; i++)
        //sendCoinAndGetTrxId("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", );

            //sendToken("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", "DucTan", 1);
        //getAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");
        //getAccount("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ");
        //sendCoin("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", 6498664766L);

        //String password1 = "lamductan@123";
        //String walletFilePath1 = "UTC--2018-06-29T06-53-22.686000000Z--TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ.json";
//        String password1 = "lamductan@123";
//        String walletFilePath1 = "UTC--2018-06-29T06-53-22.686000000Z--TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ.json";



        //sendCoin("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", password1, walletFilePath1, "TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", -10000000L);
        //sendCoin("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An", password, walletFilePath, "TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", 0L);

        //backupWallet(password1, walletFilePath1);
        //backupWallet(password, walletFilePath);

        //getBlock(398748);
        //getBlock(-1);
//        findMultiContractTransaction(0, TronUtils.getBlockCount());
        //statictisBlocks();

//        statictisBlocks();

//        Transaction transaction = sendTokenAndGetTrxId(privateKey,"TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", "DucTan",22);
//        System.out.print(TronUtils.getTransactionId(transaction));

//        Transaction transaction = sendCoinAndGetTrxId(privateKey,"TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ",22);
//        System.out.print(TronUtils.getTransactionId(transaction));

        //backupWallet(password1, walletFilePath1);

    }
}
