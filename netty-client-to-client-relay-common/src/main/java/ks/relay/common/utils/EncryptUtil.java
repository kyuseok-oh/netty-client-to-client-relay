package ks.relay.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class EncryptUtil {
  private final String salt;
  private boolean isInitialized = false;
  private String iv;
  private Key keySpec;
  private static Random random = new Random();

  /**
   * Encrypt with SHA-512.
   * 
   * @param str String to encrypt
   * @return String
   * @throws NoSuchAlgorithmException
   */
  public static String shaEncrypt(String input) throws NoSuchAlgorithmException {
    String output = "";
    StringBuilder sb = new StringBuilder();
    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(input.getBytes(StandardCharsets.UTF_8));
    byte[] msgb = md.digest();

    for (int i = 0; i < msgb.length; i++) {
      byte temp = msgb[i];
      StringBuilder str = new StringBuilder(Integer.toHexString(temp & 0xFF));
      while (str.length() < 2) {
        str.insert(0, '0');
      }
      String tmpStr = str.toString().substring(str.length() - 2);
      sb.append(tmpStr);
    }
    output = sb.toString();

    return output;
  }

  /**
   * Encrypt with AES256.
   * 
   * @param str String to encrypt
   * @return String
   * @throws GeneralSecurityException
   */
  public String aesEncrypt(String str) throws GeneralSecurityException {
    if (!isInitialized) {
      aesInit();
    }
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
    byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
    return new String(Base64.encodeBase64(encrypted), StandardCharsets.UTF_8);
  }

  private void aesInit() {
    iv = salt.substring(0, 16);
    byte[] keyBytes = new byte[16];
    byte[] b = salt.getBytes(StandardCharsets.UTF_8);
    int len = b.length;
    if (len > keyBytes.length) {
      len = keyBytes.length;
    }
    System.arraycopy(b, 0, keyBytes, 0, len);
    keySpec = new SecretKeySpec(keyBytes, "AES");
    isInitialized = true;
  }

  /**
   * Decrypt a string encrypted with AES256
   * 
   * @param str String to decrypt
   * @return String
   * @throws GeneralSecurityException
   */
  public String aesDecrypt(String str) throws GeneralSecurityException {
    if (!isInitialized) {
      aesInit();
    }
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
    byte[] byteStr = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
    return new String(c.doFinal(byteStr), StandardCharsets.UTF_8);
  }

  /**
   * Generates a random password of a given length.
   * 
   * @param length
   * @return String
   */
  public static String generateRandomPassword(int length) {
    return generateRandomPassword(length, PasswordTypes.NumberAndAlphabetAndSpecial);
  }

  /**
   * Generates a random password with a given length and pattern.
   * 
   * @param length
   * @return String
   */
  public static String generateRandomPassword(int length, PasswordTypes type) {
    if (length <= 0) {
      return "";
    }
    char[] numberCharset = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    char[] lowerCaseCharset = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
        'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    char[] upperCaseCharset = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
        'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    char[] specialCharset = new char[] {'-', '_'};
    char[] tmpCharset1;
    char[] tmpCharset2;
    char[] charset;
    switch (type) {
      case NumberAndAlphabet:
        tmpCharset1 = ArrayUtils.addAll(lowerCaseCharset, upperCaseCharset);
        charset = ArrayUtils.addAll(numberCharset, tmpCharset1);
        break;
      case NumberAndLowerCaseAlphabet:
        charset = ArrayUtils.addAll(numberCharset, lowerCaseCharset);
        break;
      case NumberAndLowerCaseAlphabetAndSpecial:
        tmpCharset1 = ArrayUtils.addAll(numberCharset, lowerCaseCharset);
        charset = ArrayUtils.addAll(tmpCharset1, specialCharset);
        break;
      case OnlyAlphabet:
        charset = ArrayUtils.addAll(lowerCaseCharset, upperCaseCharset);
        break;
      case OnlyLowerCaseAlphabet:
        charset = lowerCaseCharset;
        break;
      case OnlyNumber:
        charset = numberCharset;
        break;
      case NumberAndAlphabetAndSpecial:
      default:
        tmpCharset1 = ArrayUtils.addAll(numberCharset, lowerCaseCharset);
        tmpCharset2 = ArrayUtils.addAll(upperCaseCharset, specialCharset);
        charset = ArrayUtils.addAll(tmpCharset1, tmpCharset2);
        break;
    }
    StringBuilder sb = new StringBuilder(length);
    int charsetLength = charset.length;
    
    for (int i = 0; i < length; i++) {
      sb.append(charset[random.nextInt(charsetLength)]);
    }
    return sb.toString();
  }

  public enum PasswordTypes {
    OnlyNumber, OnlyAlphabet, OnlyLowerCaseAlphabet, NumberAndAlphabet, NumberAndLowerCaseAlphabet, NumberAndLowerCaseAlphabetAndSpecial, NumberAndAlphabetAndSpecial
  }
}
