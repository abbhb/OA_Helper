package com.qc.printers.common.ldap.utils;


import com.qc.printers.common.common.utils.RSAUtil;

public class PasswordRsaUtil {
    public static final String RSA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3kfWDkmf4o6O54vqImtC6fWutPdqZxPaSB4VC7BqatQX9/8KXsSZ2+2zd9pOhPcULY/F1QNl2XAnguMZ+b99KKkJ1IpgPD7gLzslo+okj2a0hTfzJBIQw/UOofKWWepExW1JCY3w1Ah2E1SbplsyLtXAAjh7ouV0iUhvZn+JRmwIDAQAB";

    public static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALeR9YOSZ/ijo7ni+oia0Lp9a6092pnE9pIHhULsGpq1Bf3/wpexJnb7bN32k6E9xQtj8XVA2XZcCeC4xn5v30oqQnUimA8PuAvOyWj6iSPZrSFN/MkEhDD9Q6h8pZZ6kTFbUkJjfDUCHYTVJumWzIu1cACOHui5XSJSG9mf4lGbAgMBAAECgYA9XmzjIw9iNqa2LrUN9R/BsMtOG+cYUBoUYLJC2LbeMJWDwDy4RK90yIIxRE0/cuyMbcmbpuXsZUGiIHOvckwFqK0uyF6BPeeV+QkPVmAL1wO3N8kEbMxakPbeNfFsYu/vG9esDe8jHUj65UPcGtQoGd7jQ/Jcy87kUH4UdoMcAQJBAORxhz/9+nc+06XYnXJrVmdPXi3D/szDixeurYQbU9ZrXgerJWrmq8pr8g6Nntkznyt7cR/l2x3k0VY5GSB2BZsCQQDNtrc4DXaAIAuVGN5HtpRrkLWeYDjvjRqP5psROMxoeLzXuvRcTXpBrUh1vtLoo8YXSNIpilDF887BqFqLT6QBAkEA1W40PNdfoPVz7GkbgQFD8rW2ee+6KTkwxOmQd/LIO3aInYWLKftl2XNM7cfm92tBdPCZ2oF4XM+hvXsPPMLHrQJAAhf492YTrawl0gelw38VNZ8Maic6jR2Xhp1nOJ6mXe3UpjFt6T6UnvR/h0tA5EM+ceA421lgBxO7J/dprH9MAQJAelhM7iRDXDTZOGb3K4Kk8cXOD6yc1yzKv3yqeAQlsR8vf5dFKHSbDXVqElK7RQ6RTXuHbn2Eu5uFAHSc/c3/EQ==";

    // RSA密钥长度1024
    public static final int RSA_KEY_SIZE = 1024;

    // RSA加密算法
    public static final String RSA_ALGORITHM = "RSA";

    // RSA加密填充方式
    public static final String RSA_PADDING = "RSA";

    // RSA加密字符集
    public static final String RSA_CHARSET = "UTF-8";

    // 加密
    public static String encrypt(String data) throws Exception {
        return RSAUtil.encryptedDataOnJava(RSA_PUBLIC_KEY,data);
    }

    // 解密
    public static String decrypt(String data) throws Exception {
        return RSAUtil.decryptDataOnJava(RSA_PRIVATE_KEY,data);
    }
    // main 测试
    public static void main(String[] args) {
        try {
            String data = "Hello, World!";
            String encryptedData = encrypt(data);
            System.out.println("Encrypted Data: " + encryptedData);
            String decryptedData = decrypt(encryptedData);
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
