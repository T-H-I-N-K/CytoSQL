package org.bkslab.cytosql.internal.util;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;

public class en_decrypt {

	  static String algorithm="DESede";
	  static Key key = null;
	  static Cipher cipher =null;

	  private static void init() {
		// TODO Auto-generated constructor stub
		  try{
			  if(key==null)
				  key = KeyGenerator.getInstance(algorithm).generateKey();
			  if(cipher==null)
				  cipher = Cipher.getInstance(algorithm);
		  }catch(Exception e){
			  System.out.println(e.getStackTrace());
		  }
	  }
	  
	  public static void main(String[] args) throws Exception {
	    String strEn = encrypt("vividnghean82)(;.?/!~");
	    System.out.println("encripted: "+strEn);
	    String strRecov=decrypt(strEn);
	    System.out.println("Recovered: " + strRecov);
	  }

	  public static String byteArrayToString(byte[] input) {
		  
		  String kq = "";
		  for (int i = 0; i < input.length; i++){
			  int aTemp = input[i]; 
			  kq = kq + ";" +Integer.toString(aTemp);
		  }
		  kq = kq.substring(1);
		  
		  return kq;
	  }
	  
	  public static byte[] stringToByteArray(String input) {
		  
			
		String[] strArr = input.split(";");
		byte[] kq = new byte[strArr.length];
		for (int i = 0; i < strArr.length; i++){
			kq[i] = Byte.parseByte(strArr[i]);
		}			  
		return kq;
	  }

	  public static String encrypt(String input) throws InvalidKeyException, BadPaddingException,
	      IllegalBlockSizeException {
		init();
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] inputBytes = input.getBytes();
	    byte[] bEncrypt=cipher.doFinal(inputBytes);
	    String strEncryt=byteArrayToString(bEncrypt);
	    return strEncryt;
	  }

	  public static String decrypt(String encryptedStr) throws InvalidKeyException,
	      BadPaddingException, IllegalBlockSizeException {
	    init();
	    byte[] encryptionBytes=stringToByteArray(encryptedStr);
		cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
	    String recovered = new String(recoveredBytes);
	    return recovered;
	  }
}
