package com.qc.printers.common.common.utils;

import com.qc.printers.common.common.CustomException;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtil {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";


    /** */
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /** */
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static String publicKey;
    public static String privateKey;


    /**
     * 私钥解密
     *
     * @param encryptedData
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData) throws Exception {
        return decryptByPrivateKey(privateKey, encryptedData);
    }

    /**
     * RSA 私钥解密
     * @param privateKey
     * @param encryptedData
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(String privateKey,byte[] encryptedData) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }


    /**
     * java端私钥解密
     */
    public static String decryptDataOnJava(String pKey,String data) {
        String temp = "";
        if (StringUtils.isEmpty(pKey)) {
            pKey = privateKey;
        }
        try {
            byte[] rs = Base64.decodeBase64(data);
            temp = new String(RSAUtil.decryptByPrivateKey(pKey,rs), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("请刷新重试，安全加密码已更新!");
        }
        return temp;
    }


    /**
     * 公钥加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    private static byte[] encryptByPublicKey(String pubKey,byte[] data) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(pubKey);
        Key publicK = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * java端公钥加密
     * 对应私钥为：
     * MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAI8xqZkZ/VDGi8q4oVjnj7lNu+rEsZMeBVrGHEswklFIXRPA/XkS6rAW1GUntj71nXw5xo2E1oS4atSiBRh4I00KU3PE+P56namEBjlscSLFOqAq0dlUGnhWIMKtzIP9XJSLbcmp+tWXgvWRhVq3Q9Q2Qqqj0A2A+KIhlEUaJM9xAgMBAAECgYA2CEXYVTO+eqT+MkwDyaD0ic4KaP0ep9naZl3/y0yy6izhCtY6jPZMytiLcQA2YqTx3rU66nCt9Q6uvJJSqOaclZadZ60XxLYx/cFSW7FsiQ1PgVfEuorjm2EH3Hhzs8f4L9JH4R6yJ0l4+v4MrNCzniNt5NmUjcCsnLUct9iR1wJBAODfHrItsMEwDI5/ezRc3TmowCJz7z31S0yDBEYA2Km5/P2lPsOqjmQipV50mhxDFn57VSIvqqew2uG1XYNBKTcCQQCjBBiNdINQQzLnPMGNYHcbDjEaVHZXuAMv0aAkLvF23cz3mhihSnEjCMYf70MKzPn/0Vq7Mb1brANWxbAbtoCXAkEAphTEJA7Q0+376CbJRQQtM8+pkAiWMul+4pSFTHqFit1dt6wa7gKCxfw8rMVrqOH3tBS87NHNtapODpOX7D/tAwJAWqP0YvLd8MrsitalaE6y60BA3TsJckzGuNf+CyBu8oDxbtsnxsb1kV1XjHok9OR0PWHS6TMG7un+EUlqWn5nkQJAMyIvjYpTAGxLD7jAM/zQC7HFpVT8Js48OVK8lQexFjb458pIFZiqXO3BIucKmXt5H0O8UblCsgB477YCwTvaMw==
     */
    public static String encryptedDataOnJava(String pubKey,String data) {
        if (StringUtils.isEmpty(pubKey)){
            pubKey = publicKey;
        }
        try {
            data = Base64.encodeBase64String(encryptByPublicKey(pubKey,data.getBytes()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }

    public static void createKey() {
        Map<String, Object> keyMap;
        try {
            keyMap = CreateSecrteKey.initKey();
            String pubkey = CreateSecrteKey.getPublicKey(keyMap);
            publicKey = pubkey;
            System.out.println(publicKey);
            String priKey = CreateSecrteKey.getPrivateKey(keyMap);
            privateKey = priKey;
            System.out.println(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class CreateSecrteKey {
        public static final String KEY_ALGORITHM = "RSA";
        //public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
        private static final String PUBLIC_KEY = "RSAPublicKey";
        private static final String PRIVATE_KEY = "RSAPrivateKey";

        //获得公钥
        public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
            //获得map中的公钥对象 转为key对象
            Key key = (Key) keyMap.get(PUBLIC_KEY);
            //byte[] publicKey = key.getEncoded();
            //编码返回字符串
            return encryptBASE64(key.getEncoded());
        }

        //获得私钥
        public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
            //获得map中的私钥对象 转为key对象
            Key key = (Key) keyMap.get(PRIVATE_KEY);
            //byte[] privateKey = key.getEncoded();
            //编码返回字符串
            return encryptBASE64(key.getEncoded());
        }

        //解码返回byte
        public static byte[] decryptBASE64(String key) throws Exception {
            return Base64.decodeBase64(key);
        }

        //编码返回字符串
        public static String encryptBASE64(byte[] key) throws Exception {
            return Base64.encodeBase64String(key);
        }

        //map对象中存放公私钥
        public static Map<String, Object> initKey() throws Exception {
            //获得对象 KeyPairGenerator 参数 RSA 1024个字节
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(1024);
            //通过对象 KeyPairGenerator 获取对象KeyPair
            KeyPair keyPair = keyPairGen.generateKeyPair();

            //通过对象 KeyPair 获取RSA公私钥对象RSAPublicKey RSAPrivateKey
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            //公私钥对象存入map中
            Map<String, Object> keyMap = new HashMap<String, Object>(2);
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            return keyMap;
        }

        public static void main(String[] args) {
            Map<String, Object> keyMap;
            try {
                keyMap = initKey();
                String publicKey = getPublicKey(keyMap);
                System.out.println(publicKey);
                String privateKey = getPrivateKey(keyMap);
                System.out.println(privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
