package org.mjkong.fabric.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.mjkong.fabric.sample.model.Category;
import org.mjkong.fabric.sample.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/api")
@EnableAutoConfiguration
public class StoreManager {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public static final Logger logger = LoggerFactory.getLogger(StoreManager.class);
    private static Gateway gateway;

    @RequestMapping("/")
    String home() {
        return "Hello Store2!";
    }

//  Category
    @RequestMapping(value = "/manager/category", method = RequestMethod.GET)
    public ResponseEntity<?> getCategories() {
        String resultStr = "";

        try{
            byte[] result = getContract("mymarketchannel", "marketcc").submitTransaction("getCategories", "");
            resultStr = new String(result, UTF_8);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(resultStr, HttpStatus.OK);

    }

    @RequestMapping(value = "/manager/category", method = RequestMethod.POST)
    public ResponseEntity<?> createCategory(@RequestBody Category category){
        String resultStr = "";

        try {
            byte[] result = getContract("mymarketchannel", "marketcc").submitTransaction("registCategory", category.getId(), category.getDesc());
            resultStr = new String(result, UTF_8);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<String>(resultStr, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/manager/product/all", method = RequestMethod.GET)
    public ResponseEntity<?> getProductList() {
        String resultStr = "";

        try {

            byte[] result = getContract("mymarketchannel", "marketcc").submitTransaction("getProductList", "" );
            resultStr = new String(result, UTF_8);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(resultStr, HttpStatus.OK);
    }

    @RequestMapping(value = "/manager/product/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getProduct(@PathVariable String id) {
        String resultStr = "";

        try {

            byte[] result = getContract("mymarketchannel", "marketcc").submitTransaction("getProduct", id);
            resultStr = new String(result, UTF_8);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(resultStr, HttpStatus.OK);
    }

    @RequestMapping(value = "/manager/product", method = RequestMethod.POST)
    public ResponseEntity<?> createProduct(@RequestBody Product product){
        String resultStr = "";

        try {
            byte[] result = getContract("mymarketchannel", "marketcc").submitTransaction("registProducts", product.getName(), product.getQty(), product.getOwner());
            resultStr = new String(result, UTF_8);

        }catch(Exception e){
            e.printStackTrace();;
        }

        return new ResponseEntity<>(resultStr, HttpStatus.CREATED);
    }


    private Contract getContract(String channelId, String chaincodeId) throws Exception {

        getGateway();
        Network network = gateway.getNetwork(channelId);
        Contract contract = network.getContract(chaincodeId);

        return contract;
    }

    private Gateway getGateway() throws Exception {
        if(gateway != null){
            return gateway;
        }

        return connectToBlockchain();
    }

    public Gateway connectToBlockchain() throws Exception {

        logger.info("Start to connect Gateway");

        Path projectRoot = Paths.get("/","Users","mjkong","Dev", "projects","mymarket","app","store2");

        Path walletPath = projectRoot.resolve(Paths.get("wallet"));
        logger.info("wallet path: " + walletPath.toString());
        Wallet wallet = Wallet.createFileSystemWallet(walletPath);

        Path networkConfigPath = projectRoot.resolve(Paths.get( "config", "mymarketStore2Connection.json"));
        logger.info("Config Path : " + networkConfigPath.toString());

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "User1@store2.mymarket.com").networkConfig(networkConfigPath).discovery(true);

        logger.info("builder connect");
        gateway = builder.connect();

        return gateway;
    }

    public static void main(String... args) {
        SpringApplication.run(StoreManager.class, args);
    }
}
