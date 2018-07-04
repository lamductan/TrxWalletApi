package org.tronUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.TransactionUtils;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.Credentials;
import org.tron.keystore.StringUtils;
import org.tron.keystore.Wallet;
import org.tron.keystore.WalletFile;
import org.tron.keystore.WalletUtils;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.walletcli.Client;
import org.tron.walletcli.Test;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletClient;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.protos.Contract;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import static org.tron.keystore.WalletUtils.loadCredentials;

public class TrxWallet {
    //private static Client client = new Client();
    private static GrpcClient rpcCli = WalletClient.init();

    public static String registerWallet(String passwordStr) throws CipherException, IOException  {
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

    public static Block getBlock(long blockNum) {
       return WalletClient.GetBlock(blockNum);
    }

    public static Optional<Transaction> getTransactionById(String transactionId) {
        return WalletClient.getTransactionById(transactionId);
    }

    /**
     * Print information of all blocks in range(startBlockNum, endBlockNum) inclusive.
     * @param startBlockNum  a number giving height of start block
     * @param endBlockNum  a number giving height of end block
     */
    public static Optional<BlockList> getBlockByLimitNext(long startBlockNum, long endBlockNum) {
        return WalletClient.getBlockByLimitNext(startBlockNum, endBlockNum);
    }

    /**
     * Print information of all latest num blocks.
     * @param num  a number giving number of latest blocks
     */
    public static Optional<BlockList> getBlockByLatestNum(long num) {
        return WalletClient.getBlockByLatestNum(num);
    }

    /**
     * Print information of account.
     * @param accountAddress  a string giving address of account
     */
    public static Account getAccount(String accountAddress) {
        byte[] addressBytes = WalletClient.decodeFromBase58Check(accountAddress);
        return WalletClient.queryAccount(addressBytes);
    }

    public static boolean sendCoin(String fromAddress, String password, String walletFilePath, String toAddress, long amount) {
        byte[] owner = WalletClient.decodeFromBase58Check(fromAddress);
        byte[] to = WalletClient.decodeFromBase58Check(toAddress);
        Contract.TransferContract contract = WalletClient.createTransferContract(to, owner, amount);
        Transaction transaction = rpcCli.createTransaction(contract);
        if (transaction == null) {
            System.out.println("Transaction null");
            return false;
        }
        else if (transaction.getRawData().getContractCount() == 0) {
            Logger logger = LoggerFactory.getLogger("TestClient");
            logger.info(Utils.printTransaction(transaction));
            System.out.println("transaction.getRawData().getContractCount() == 0");
            return false;
        }
        try {
            transaction = TransactionUtils.sign(transaction, getEcKey(password, walletFilePath));
        } catch (Exception e) {
            System.out.println(e);
        }
        return rpcCli.broadcastTransaction(transaction);
    }

    private static ECKey getEcKey(String password, String walletFilePath) throws CipherException, IOException {
        byte[] passwd = StringUtils.char2Byte(password.toCharArray());
        String filePath = "Wallet/" + walletFilePath;
        File wallet = new File(filePath);
        Credentials credentials = loadCredentials(passwd, wallet);
        WalletFile walletFile = Wallet.createStandard(passwd, credentials.getEcKeyPair());
        return Wallet.decrypt(passwd, walletFile);
    }
}
