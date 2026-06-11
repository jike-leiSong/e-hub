package cn.sl.ehub.common.utils;

import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
public class RSAUtil {
    public static final String CHARSET = "UTF-8";
    public static final String RSA_ALGORITHM = "RSA";

    /**
     * 得到公私钥对字符串
     *
     * @param keySize
     * @return
     */
    public static Map<String, String> createKeys(int keySize) throws NoSuchAlgorithmException {
        Map<String, String> keyPairMap = new HashMap<>();
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        //初始化KeyPairGenerator对象，密钥长度
        kpg.initialize(keySize);
        //生成密钥对
        KeyPair keyPair = kpg.generateKeyPair();

        //得到公钥
        Key publicKey = keyPair.getPublic();
        //公钥Base64编码
        String publicKeyStr = Base64.encode(publicKey.getEncoded());
        //String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        log.info("keyMap公钥Base64编码:" + publicKeyStr);
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        //私钥Base64编码
        String privateKeyStr = Base64.encode(privateKey.getEncoded());
        //String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        log.info("keyMap私钥Base64编码:" + privateKeyStr);
        keyPairMap.put("publicKey", publicKeyStr);
        keyPairMap.put("privateKey", privateKeyStr);
        return keyPairMap;
    }

    /**
     * 得到公钥
     *
     * @param publicKeyStr
     * @return
     */
    public static RSAPublicKey getPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey rsaPublicKey = null;

        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //通过X509编码的Key指令获得公钥对象
        X509EncodedKeySpec pkcs8EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKeyStr));
        //X509EncodedKeySpec pkcs8EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
        rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(pkcs8EncodedKeySpec);
        log.info("返回公钥Base64编码:" + rsaPublicKey);
        return rsaPublicKey;
    }

    /**
     * 得到私钥
     *
     * @param privateKeyStr
     * @return
     */
    public static RSAPrivateKey getPrivateKey(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateKey rsaPrivateKey = null;
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //通过PKCS#8编码的Key指令获得私钥对象
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyStr));
        //PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
        rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        log.info("返回私钥Base64编码:" + rsaPrivateKey);
        return rsaPrivateKey;
    }

    /**
     * 公钥加密
     * 每次加密的字节数，不能超过密钥的长度值减去11
     *
     * @param data
     * @param rsaPublicKey
     * @return
     */
    public static String publicKeyEncrypt(String data, RSAPublicKey rsaPublicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return Base64.encode(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), rsaPublicKey.getModulus().bitLength()));
        //return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), rsaPublicKey.getModulus().bitLength()));
    }

    public static String publicKeyEncrypt(String data, String rsaPublicKeyStr) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException {

        RSAPublicKey rsaPublicKey = RSAUtil.getPublicKey(rsaPublicKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return Base64.encode(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), rsaPublicKey.getModulus().bitLength()));
    }

    /**
     * 私钥解密
     *
     * @param data
     * @param rsaPrivateKey
     * @return
     */
    public static String privateKeyDecrypt(String data, RSAPrivateKey rsaPrivateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(data), rsaPrivateKey.getModulus().bitLength()));
    }

    public static String privateKeyDecrypt(String data, String rsaPrivateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {

        RSAPrivateKey rsaPrivateKey = RSAUtil.getPrivateKey(rsaPrivateKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(data), rsaPrivateKey.getModulus().bitLength()));

    }


    /**
     * 私钥加密
     *
     * @param data
     * @param rsaPrivateKey
     * @return
     */
    public static String privateKeyEncrypt(String data, RSAPrivateKey rsaPrivateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
        return Base64.encode(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), rsaPrivateKey.getModulus().bitLength()));
    }

    public static String privateKeyEncrypt(String data, String rsaPrivateKeyStr) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

        RSAPrivateKey rsaPrivateKey = RSAUtil.getPrivateKey(rsaPrivateKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
        return Base64.encode(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), rsaPrivateKey.getModulus().bitLength()));
    }

    /**
     * 公钥解密
     *
     * @param data
     * @param rsaPublicKey
     * @return
     */
    public static String publicKeyDecrypt(String data, RSAPublicKey rsaPublicKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, IOException {

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(data), rsaPublicKey.getModulus().bitLength()));

    }

    public static String publicKeyDecrypt(String data, String rsaPublicKeyStr) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        RSAPublicKey rsaPublicKey = RSAUtil.getPublicKey(rsaPublicKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(data), rsaPublicKey.getModulus().bitLength()));
    }

    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) throws IllegalBlockSizeException, BadPaddingException, IOException {
        int maxBlock = 0;
        if (opmode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        while (datas.length > offSet) {
            if (datas.length - offSet > maxBlock) {
                buff = cipher.doFinal(datas, offSet, maxBlock);
            } else {
                buff = cipher.doFinal(datas, offSet, datas.length - offSet);
            }
            out.write(buff, 0, buff.length);
            i++;
            offSet = i * maxBlock;
        }
        byte[] resultDatas = out.toByteArray();
        out.close();
        return resultDatas;
    }
}
