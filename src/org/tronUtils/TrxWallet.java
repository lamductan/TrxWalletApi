package org.tronUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.keystore.WalletFile;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.api.GrpcAPI.BlockList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class TrxWallet {
    private static Client client = new Client();

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

    /*
    public void sendCoin(String toAddress, long amount) throws IOException, CipherException, CancelException {
        boolean result = (client != null && isLogin);
        if (result) {
            byte[] to = WalletClient.decodeFromBase58Check(toAddress);
            if (to == null) {
                result = false;
            } else {
                System.out.println();
                wallet.sendCoin(to, amount);
            }
        }
        if (result) {
            logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
        } else {
            logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
        }
    }
    */

    public static void main(String[] args) {
        String password = "tronUTS123";
        String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";
    }
}
