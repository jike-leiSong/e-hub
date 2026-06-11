package cn.sl.ehub.common.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * @Description: AES 加解密工具
 * @Author sl
 * @Date 2026-05-28
 */
public class AESUtil {

    private static final String CIPHER_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    /**
     * 初始化key
     *
     * @return
     * @throws Exception
     */
    public static byte[] initKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(128);
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 通过密钥和盐加密正文
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
        Key k = new SecretKeySpec(key, KEY_ALGORITHM);
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, k, paramSpec);
        return cipher.doFinal(data);
    }

    /**
     * 通过密钥和盐解密密文
     *
     * @param bytes
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] bytes, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
        Key k = new SecretKeySpec(key, KEY_ALGORITHM);
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, k, paramSpec);
        return cipher.doFinal(bytes);
    }

    /**
     * BASE64 编码
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static String encodeToBase64String(String data, byte[] key, byte[] iv) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(data.getBytes(), key, iv));
    }

    public static String encodeToBase64String(String data, String key, String iv) throws Exception {
        return Base64.getEncoder().encodeToString(encrypt(data.getBytes(), Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv)));
    }

    /**
     * BASE64 解码
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static String decodeFromBase64String(String data, byte[] key, byte[] iv) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data);
        return new String(decrypt(bytes, key, iv));
    }

    public static String decodeFromBase64String(String data, String key, String iv) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data);
        return new String(decrypt(bytes, Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv)));
    }

    public static void main(String[] args) throws Exception {

        String key = Base64.getEncoder().encodeToString(AESUtil.initKey());
        System.out.println("key:" + key);
        String iv = Base64.getEncoder().encodeToString(AESUtil.initKey());
        System.out.println("iv:" + iv);

        String content = "ouyushanytyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy";
        String cipher = AESUtil.encodeToBase64String(content, key, iv);
        System.out.println("cipher:" + cipher);

        String plain = AESUtil.decodeFromBase64String(cipher, key, iv);
        System.out.println("plain:" + plain);
    }
}
