package org.blockchain;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.TransactionUtils;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CipherException;
import org.tron.keystore.Credentials;
import org.tron.keystore.StringUtils;
import org.tron.keystore.Wallet;
import org.tron.keystore.WalletFile;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletClient;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;

import static org.tron.keystore.WalletUtils.loadCredentials;

public class TronUtils {
    //private static Client client = new Client();
    private static GrpcClient rpcCli = WalletClient.init();

    public static String registerWallet(String passwordStr) throws CipherException, IOException {
        char[] password = passwordStr.toCharArray();
        if (!WalletClient.passwordValid(password)) {
            return null;
        }
        byte[] passwd = StringUtils.char2Byte(password);
        WalletClient wallet = new WalletClient(passwd);
        StringUtils.clear(passwd);

        String keystoreName = wallet.store2Keystore();
        return keystoreName;
    }
    public static String registerAccount(String passwordStr) throws CipherException, IOException {
        char[] password = passwordStr.toCharArray();
        if (!WalletClient.passwordValid(password)) {
            return null;
        }
        byte[] passwd = StringUtils.char2Byte(password);
        WalletClient wallet = new WalletClient(passwd);
        String privKey = wallet.getEcKey(passwd).getPrivKey().toString(16);
        StringUtils.clear(passwd);
        return privKey;
    }
    public static String getAddressFromPrivKey(String privKey)
    {
        BigInteger bi = new BigInteger(privKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        return WalletClient.encode58Check(ecKey.getAddress());
    }
    public static Block getBlock(long blockNum) {
        return WalletClient.GetBlock(blockNum);
    }

    public static long getBlockCount() {
        return getBlock(-1).getBlockHeader().getRawData().getNumber();
    }

    public static Optional<Transaction> getTransactionById(String transactionId) {
        return WalletClient.getTransactionById(transactionId);
    }

    /**
     * Print information of all blocks in range(startBlockNum, endBlockNum) inclusive.
     *
     * @param startBlockNum a number giving height of start block
     * @param endBlockNum   a number giving height of end block
     */
    public static Optional<BlockList> getBlockByLimitNext(long startBlockNum, long endBlockNum) {
        return WalletClient.getBlockByLimitNext(startBlockNum, endBlockNum);
    }

    /**
     * Print information of all latest num blocks.
     *
     * @param num a number giving number of latest blocks
     */
    public static Optional<BlockList> getBlockByLatestNum(long num) {
        return WalletClient.getBlockByLatestNum(num);
    }
    /**
     * Print information of account.
     *
     * @param accountAddress a string giving address of account
     */
    public static Account getAccount(String accountAddress) {
        byte[] addressBytes = WalletClient.decodeFromBase58Check(accountAddress);
        return WalletClient.queryAccount(addressBytes);
    }

    public static long getBalance(String address){
        Account account = getAccount(address);
        return account.getBalance();
    }

    public static boolean sendCoinFromFileAndPassword(String fromAddress, String password, String walletFilePath, String toAddress, long amount) {

        try {
            ECKey ecKey = getEcKey(password, walletFilePath);
            return sendCoinFromPrivKey(fromAddress, ecKey, toAddress, amount);
        } catch (Exception e) {
            System.out.println("wrong sendcoin");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendTokenFromFileAndPassword(String fromAddress, String password, String walletFilePath, String toAddress, String tokenName, long amount) {

        try {
            ECKey ecKey = getEcKey(password, walletFilePath);
            return sendTokenFromPrivKey(fromAddress, ecKey, toAddress,tokenName,amount);
        } catch (Exception e) {
            System.out.println("wrong sendcoin");
            e.printStackTrace();
        }
        return false;
    }

    public static Transaction sendTokenFromFileAndPasswordAndGetTrxId(String fromAddress, String password, String walletFilePath, String toAddress, String tokenName, long amount) {

        try {
            ECKey ecKey = getEcKey(password, walletFilePath);
            return sendTokenFromPrivKeyAndGetTrxId(fromAddress, ecKey, toAddress,tokenName,amount);
        } catch (Exception e) {
            System.out.println("wrong sendcoin");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean sendAll(String privateKey, String toAddress)
    {
        long amount = getBalance(getAddressFromPrivKey(privateKey));
        if(sendCoin(privateKey, toAddress, amount))
            return true;
        else {
            long fee = getTransactionFee(privateKey, toAddress, amount);
            amount -= fee;
            return sendCoin(privateKey, toAddress, amount);
        }
    }

    public static boolean sendAllToken(String privateKey, String toAddress, String tokenName)
    {
        String address = getAddressFromPrivKey(privateKey);
        Account account = TronUtils.getAccount(address);
        if (account.getAssetCount() <= 0) {
            return false;
        }
        boolean findTokenName = false;
        for(String name : account.getAssetMap().keySet()) {
            if (name.equals(tokenName)) {
                findTokenName = true;
                break;
            }
        }
        if (findTokenName == false) {
            return false;
        }
        else {
            long amount = TronUtils.getAccount(address).getAssetMap().get(tokenName);
            return (sendToken(privateKey, toAddress, tokenName, amount));
        }
    }

    public static long getTransactionFee(String privateKey, String toAddress, long amount)
    {
        BigInteger bi = new BigInteger(privateKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        byte[] owner = ecKey.getAddress();
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        Contract.TransferContract contract = WalletClient.createTransferContract(to, owner, amount);
        Transaction transaction = rpcCli.createTransaction(contract);

        if (transaction == null) {
            return 0;
        } else if (transaction.getRawData().getContractCount() == 0) {
            return 0;
        }
        try {
            transaction = TransactionUtils.sign(transaction, ecKey);
        } catch (Exception e) {
            System.out.println(e);
        }
        return transaction.toByteArray().length*10;
    }

    public static Transaction sendCoinFromFileAndPasswordAndGetTrxId(String fromAddress, String password, String walletFilePath, String toAddress, long amount) {

        try {
            ECKey ecKey = getEcKey(password, walletFilePath);
            return sendCoinFromPrivKeyAndGetTrxId(fromAddress, ecKey, toAddress,amount);
        } catch (Exception e) {
            System.out.println("wrong sendcoin");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean sendCoinFromPrivKey(String fromAddress, ECKey ecKey, String toAddress, long amount) {
        if(!WalletClient.encode58Check(ecKey.getAddress()).equals(fromAddress)){
            System.out.println("from priv key: " + WalletClient.encode58Check(ecKey.getAddress()));
            System.out.println("address: " + WalletClient.decodeFromBase58Check(fromAddress).toString());
            System.out.println("address input is not map with private key");
            return false;
        }
        return sendCoin(ecKey, toAddress, amount);
    }

    public static boolean sendTokenFromPrivKey(String fromAddress, ECKey ecKey, String toAddress, String tokenName, long amount) {
        if(!WalletClient.encode58Check(ecKey.getAddress()).equals(fromAddress)){
            System.out.println("from priv key: " + WalletClient.encode58Check(ecKey.getAddress()));
            System.out.println("address: " + WalletClient.decodeFromBase58Check(fromAddress).toString());
            System.out.println("address input is not map with private key");
            return false;
        }
        return sendToken(ecKey, toAddress,tokenName, amount);
    }

    public static Transaction sendTokenFromPrivKeyAndGetTrxId(String fromAddress, ECKey ecKey, String toAddress, String tokenName, long amount) {
        if(!WalletClient.encode58Check(ecKey.getAddress()).equals(fromAddress)){
            System.out.println("from priv key: " + WalletClient.encode58Check(ecKey.getAddress()));
            System.out.println("address: " + WalletClient.decodeFromBase58Check(fromAddress).toString());
            System.out.println("address input is not map with private key");
            return null;
        }
        return sendTokenAndGetTrxId(ecKey, toAddress,tokenName, amount);
    }

    public static Transaction sendCoinFromPrivKeyAndGetTrxId(String fromAddress, ECKey ecKey, String toAddress,  long amount) {
        if(!WalletClient.encode58Check(ecKey.getAddress()).equals(fromAddress)){
            System.out.println("from priv key: " + WalletClient.encode58Check(ecKey.getAddress()));
            System.out.println("address: " + WalletClient.decodeFromBase58Check(fromAddress).toString());
            System.out.println("address input is not map with private key");
            return null;
        }
        return sendCoinAndGetTrxId(ecKey, toAddress, amount);
    }

    public static Transaction sendTokenAndGetTrxId(ECKey ecKey, String toAddress, String tokenName, long amount) {
        byte[] owner = ecKey.getAddress();
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        byte[] assetName = tokenName.getBytes();
        Contract.TransferAssetContract assetContract = WalletClient.createTransferAssetContract(to,assetName,owner,amount);
        Transaction transaction = rpcCli.createTransferAssetTransaction(assetContract);

        if (transaction == null) {
            System.out.println("Transaction null");
            return null;
        } else if (transaction.getRawData().getContractCount() == 0) {
            Logger logger = LoggerFactory.getLogger("TestClient");
            logger.info(Utils.printTransaction(transaction));
            System.out.println("transaction.getRawData().getContractCount() == 0");
            return null;
        }
        try {
            transaction = TransactionUtils.sign(transaction, ecKey);
        } catch (Exception e) {
            System.out.println(e);
        }
        if(rpcCli.broadcastTransaction(transaction)) return transaction;
        return null;
    }

    public static Transaction sendCoinAndGetTrxId(ECKey ecKey, String toAddress, long amount) {
        byte[] owner = ecKey.getAddress();
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        Contract.TransferContract contract = WalletClient.createTransferContract(to,owner,amount);
        Transaction transaction = rpcCli.createTransaction(contract);

        if (transaction == null) {
            System.out.println("Transaction null");
            return null;
        } else if (transaction.getRawData().getContractCount() == 0) {
            Logger logger = LoggerFactory.getLogger("TestClient");
            logger.info(Utils.printTransaction(transaction));
            System.out.println("transaction.getRawData().getContractCount() == 0");
            return null;
        }
        try {
            transaction = TransactionUtils.sign(transaction, ecKey);
        } catch (Exception e) {
            System.out.println(e);
        }
        if(rpcCli.broadcastTransaction(transaction)) return transaction;
        return null;
    }

    public static Transaction sendTokenAndGetTrxId(String privateKey, String toAddress, String tokenName, long amount) {
        BigInteger bi = new BigInteger(privateKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        return sendTokenAndGetTrxId(ecKey,toAddress,tokenName,amount);
    }

    public static Transaction sendCoinAndGetTrxId(String privateKey, String toAddress, long amount) {
        BigInteger bi = new BigInteger(privateKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        return sendCoinAndGetTrxId(ecKey,toAddress,amount);
    }

    public static boolean sendToken(ECKey ecKey, String toAddress, String tokenName, long amount) {
        byte[] owner = ecKey.getAddress();
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        byte[] assetName = tokenName.getBytes();
        Contract.TransferAssetContract assetContract = WalletClient.createTransferAssetContract(to,assetName,owner,amount);
        Transaction transaction = rpcCli.createTransferAssetTransaction(assetContract);

        if (transaction == null) {
            System.out.println("Transaction null");
            return false;
        } else if (transaction.getRawData().getContractCount() == 0) {
            Logger logger = LoggerFactory.getLogger("TestClient");
            logger.info(Utils.printTransaction(transaction));
            System.out.println("transaction.getRawData().getContractCount() == 0");
            return false;
        }
        try {
            transaction = TransactionUtils.sign(transaction, ecKey);
        } catch (Exception e) {
            System.out.println(e);
        }
        return rpcCli.broadcastTransaction(transaction);
    }

    public static boolean sendCoin(ECKey ecKey, String toAddress, long amount) {
        byte[] owner = ecKey.getAddress();
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        Contract.TransferContract contract = WalletClient.createTransferContract(to, owner, amount);
        Transaction transaction = rpcCli.createTransaction(contract);

        if (transaction == null) {
            System.out.println("Transaction null");
            return false;
        } else if (transaction.getRawData().getContractCount() == 0) {
            Logger logger = LoggerFactory.getLogger("TestClient");
            logger.info(Utils.printTransaction(transaction));
            System.out.println("transaction.getRawData().getContractCount() == 0");
            return false;
        }
        try {
            transaction = TransactionUtils.sign(transaction, ecKey);
        } catch (Exception e) {
            System.out.println(e);
        }
        return rpcCli.broadcastTransaction(transaction);
    }

    public static boolean sendToken(String privateKey, String toAddress, String tokenName, long amount) {
        BigInteger bi = new BigInteger(privateKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        return  sendToken(ecKey,toAddress,tokenName,amount);
    }

    public static boolean sendCoin(String privateKey, String toAddress, long amount) {
        BigInteger bi = new BigInteger(privateKey,16);
        ECKey ecKey = ECKey.fromPrivate(bi);
        return  sendCoin(ecKey,toAddress,amount);
    }

    private static ECKey getEcKey(String password, String walletFilePath) throws CipherException, IOException {
        byte[] passwd = StringUtils.char2Byte(password.toCharArray());
        String filePath = "Wallet/" + walletFilePath;
        File wallet = new File(filePath);
        Credentials credentials = loadCredentials(passwd, wallet);
        WalletFile walletFile = Wallet.createStandard(passwd, credentials.getEcKeyPair());
        return Wallet.decrypt(passwd, walletFile);
    }

    public static String backUpWallet(String password, String walletFilePath)    {
        ECKey ecKey = null;
        try {
            ecKey = getEcKey(password, walletFilePath);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String privKey = ByteArray.toHexString(ecKey.getPrivKey().toByteArray());
        int len = privKey.length();
        if (len > 64)
            privKey = privKey.substring(len - 64);
        return privKey;
    }

    public static String getTransactionId(Transaction transaction) {
        return ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    }

    public static String getTransactionHash(Transaction transaction) {
        return ByteArray.toHexString(Sha256Hash.hash(transaction.toByteArray()));
    }

    // get Transaction Amount made by Trong-Dat Phan
    public static long getTransactionAmount(Transaction transaction) {
        long totalAmount = 0;
        List<Transaction.Contract> listContract = transaction.getRawData().getContractList();
        for (int i = 0; i < listContract.size(); i++) {
            Transaction.Contract contract = listContract.get(i);
            try {
                totalAmount += TronUtils.getContractAmount(contract);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return totalAmount;
    }

    public static String getContractOwner(Transaction.Contract contract) throws InvalidProtocolBufferException {
        Transaction.Contract.ContractType contractType = contract.getType();
        switch (contractType) {
            case TransferContract:
                Contract.TransferContract transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
                return WalletClient.encode58Check(transferContract.getOwnerAddress().toByteArray());
            case TransferAssetContract:
                Contract.TransferAssetContract transferAssetContract = contract.getParameter().unpack(Contract.TransferAssetContract.class);
                return WalletClient.encode58Check(transferAssetContract.getOwnerAddress().toByteArray());
            default:
                return null;
        }
    }

    public static String getContractToAddress(Transaction.Contract contract) throws InvalidProtocolBufferException {
        Transaction.Contract.ContractType contractType = contract.getType();
        switch (contractType) {
            case TransferContract:
                Contract.TransferContract transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
                return WalletClient.encode58Check(transferContract.getToAddress().toByteArray());
            case TransferAssetContract:
                Contract.TransferAssetContract transferAssetContract = contract.getParameter().unpack(Contract.TransferAssetContract.class);
                return WalletClient.encode58Check(transferAssetContract.getToAddress().toByteArray());
            default:
                return null;
        }
    }

    public static long getContractAmount(Transaction.Contract contract) throws InvalidProtocolBufferException {
        Transaction.Contract.ContractType contractType = contract.getType();
        switch (contractType) {
            case TransferContract:
                Contract.TransferContract transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
                return transferContract.getAmount();
            case TransferAssetContract:
                Contract.TransferAssetContract transferAssetContract = contract.getParameter().unpack(Contract.TransferAssetContract.class);
                return transferAssetContract.getAmount();
            default:
                return -1;
        }
    }

    public static String getAssetContractName(Contract.TransferAssetContract transferAssetContract)  {
        return new String(transferAssetContract.getAssetName().toByteArray(),
                Charset.forName("UTF-8"));
    }


}