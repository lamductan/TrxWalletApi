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
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.tron.keystore.WalletUtils.loadWalletFile;

public class TrxWallet {
    private static final Logger logger = LoggerFactory.getLogger("TestClient");
    private Client client = new Client();
    private boolean isLogin = false;
    private WalletClient wallet;

    /**
     * Print information of block in terminal.
     * @param blockNum  a number giving height of this block
     */
    public Protocol.Block GetBlock(long blockNum) {
        Protocol.Block block = WalletClient.getBlock(blockNum);
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

    public char[] inputPassword2Twice() throws IOException {
        char[] password0;
        while (true) {
            System.out.println("Please input password.");
            password0 = Utils.inputPassword(true);
            System.out.println("Please input password again.");
            char[] password1 = Utils.inputPassword(true);
            boolean flag = Arrays.equals(password0, password1);
            StringUtils.clear(password1);
            if (flag) {
                break;
            }
            System.out.println("The passwords do not match, please input again.");
        }
        return password0;
    }

    public byte[] inputPrivateKey() throws IOException {
        byte[] temp = new byte[128];
        byte[] result = null;
        System.out.println("Please input private key.");
        while (true) {
            int len = System.in.read(temp, 0, temp.length);
            if (len >= 64) {
                byte[] privateKey = Arrays.copyOfRange(temp, 0, 64);
                result = StringUtils.hexs2Bytes(privateKey);
                StringUtils.clear(privateKey);
                if (WalletClient.priKeyValid(result)) {
                    break;
                }
            }
            StringUtils.clear(result);
            System.out.println("Invalid private key, please input again.");
        }
        StringUtils.clear(temp);
        return result;
    }

    public void importWallet() throws CipherException, IOException {
        char[] password = inputPassword2Twice();
        byte[] priKey = inputPrivateKey();

        String fileName = client.importWallet(password, priKey);
        StringUtils.clear(password);
        StringUtils.clear(priKey);

        if (null == fileName) {
            System.out.println("Import wallet failed !!");
            return;
        }
        System.out.println("Import a wallet successful, keystore file name is " + fileName);
    }

    public String importWallet(String password, String privateKey) throws CipherException, IOException {
        char[] password_cstr = password.toCharArray();
        byte[] priKey = StringUtils.char2Byte(privateKey.toCharArray());
        return client.importWallet(password_cstr, priKey);
    }


    public void login() throws IOException, CipherException {
        System.out.println("Please input your password.");
        char[] password = Utils.inputPassword(false);

        boolean result = client.login(password);
        StringUtils.clear(password);

        if (result) {
            System.out.println("Login successful !!!");
        } else {
            System.out.println("Login failed !!!");
        }
    }

    public void login(String password, String walletFilePath) throws IOException, CipherException {
        char[] password_cstr = password.toCharArray();
        String filePath = "Wallet/" + walletFilePath;
        File file = new File(filePath);
        WalletFile walletFile = loadWalletFile(file);
        wallet = new WalletClient(walletFile);
        byte[] passwd = StringUtils.char2Byte(password_cstr);
        boolean result = wallet.checkPassword(passwd);
        if (result) {
            System.out.println("Login successful !!!");
            wallet.setLogin();
        } else {
            System.out.println("Login failed !!!");
        }
        StringUtils.clear(passwd);

    }

    public void logout() {
        wallet.logout();
        System.out.println("Logout successful !!!");
    }

    public void GetAssetIssueByAccount(String addressAccount) {
        byte[] addressBytes = WalletClient.decodeFromBase58Check(addressAccount);
        if (addressBytes == null) {
            return;
        }

        Optional<GrpcAPI.AssetIssueList> result = WalletClient.getAssetIssueByAccount(addressBytes);
        if (result.isPresent()) {
            GrpcAPI.AssetIssueList assetIssueList = result.get();
            logger.info(Utils.printAssetIssueList(assetIssueList));
        } else {
            logger.info("GetAssetIssueByAccount " + " failed !!");
        }
    }

    public void CreateAccount(String address) throws CipherException, IOException, CancelException {
        boolean result = client.createAccount(address);
        if (result) {
            logger.info("CreateAccount " + " successful !!");
        } else {
            logger.info("CreateAccount " + " failed !!");
        }
    }

    public void registerWallet() throws CipherException, IOException {
        char[] password = inputPassword2Twice();
        String fileName = client.registerWallet(password);
        StringUtils.clear(password);

        if (null == fileName) {
            logger.info("Register wallet failed !!");
            return;
        }
        logger.info("Register a wallet successful, keystore file name is " + fileName);
    }

    public void registerWallet(String password) throws CipherException, IOException {
        char[] password_cstr = password.toCharArray();
        String fileName = client.registerWallet(password_cstr);
        StringUtils.clear(password_cstr);

        if (null == fileName) {
            logger.info("Register wallet failed !!");
            return;
        }
        logger.info("Register a wallet successful, keystore file name is " + fileName);
    }

    public String getEcKey(String password, String walletFilePath) throws CipherException, IOException {
        byte[] passwd = StringUtils.char2Byte(password.toCharArray());
        String filePath = "Wallet/" + walletFilePath;
        File file = new File(filePath);
        WalletFile walletFile = loadWalletFile(file);
        WalletClient walletClient = new WalletClient(walletFile);
        ECKey ecKey = walletClient.getEcKey(passwd);
        return ecKey.toString();
    }

    public String getPrivateKey(String password, String walletFilePath) throws CipherException, IOException {
        byte[] passwd = StringUtils.char2Byte(password.toCharArray());
        String filePath = "Wallet/" + walletFilePath;
        File file = new File(filePath);
        WalletFile walletFile = loadWalletFile(file);
        WalletClient walletClient = new WalletClient(walletFile);
        byte[] privateKey = walletClient.getPrivateBytes(passwd);
        return ByteUtil.toHexString(privateKey);
    }

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

    public static void main(String[] args) {
        TrxWallet trx = new TrxWallet();
        //wallet.GetBlock(58750);
        //wallet.GetTransactionById("f35350480a219cb8f0eaccde8b4a32ab7d45a6255ec34d515df691c40cda6790");
        //wallet.GetBlockByLimitNext(78800, 78802);
        //wallet.GetBlockByLatestNum(3);
        //wallet.GetAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");

        /*
        try {
            wallet.login();
        } catch(IOException e) {
            System.out.println(e);
        } catch (CipherException e) {
            System.out.println(e);
        }


        try {
            wallet.CreateAccount("TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An");
        } catch(CipherException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } catch (CancelException e) {
            System.out.println(e);
        }
         */

        /*
        try {
            wallet.importWallet();
        } catch (CipherException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        */


        /*
        try {
            System.out.println(wallet.getEcKey("lamductan@1234".toCharArray()));
        } catch (CipherException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        */
        String password = "tronUTS123";
        String walletFilePath = "UTC--2018-06-28T07-51-35.623000000Z--TKA6RhDiCy5uASGoD1cvdD37NeRsr7L8An.json";

        try {
            trx.login(password, walletFilePath);
        } catch(IOException e) {
            System.out.println(e);
        } catch (CipherException e) {
            System.out.println(e);
        }

        try {
            System.out.println(trx.getPrivateKey(password, walletFilePath));
        } catch (CipherException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

        if (trx.wallet.isLoginState()) {
            System.out.println("Is login");
        } else {
            System.out.println("Not login");
        }

        try {
            trx.sendCoin("TVEZkb74GxXkp3Sxk5AzozoyYCkEJFUswZ", 1000000);
        } catch (IOException e) {
            System.out.println(e);
        } catch (CipherException e) {
            System.out.println(e);
        } catch (CancelException e) {
            System.out.println(e);
        }

        trx.logout();
    }
}
