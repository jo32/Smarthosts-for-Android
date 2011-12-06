package us.bandj.jo.SmartHostAndroid;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

public class DESCipher {

	Key key;
	private Cipher encrypter;
	private Cipher decrypter;

	public DESCipher(String keyString) {
		setKey(DESCipher.generateKey(keyString));
		try {
			Cipher encrypter = Cipher.getInstance("DES");
			encrypter.init(Cipher.ENCRYPT_MODE, getKey());
			setEncrypter(encrypter);
			Cipher decrypter = Cipher.getInstance("DES");
			decrypter.init(Cipher.DECRYPT_MODE, getKey());
			setDecrypter(decrypter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Key generateKey(String keyString) {
		Key key = null;
		try {
			KeyGenerator generator = KeyGenerator.getInstance("DES");
			generator.init(new SecureRandom(keyString.getBytes()));
			key = generator.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}

	public CipherInputStream getEncryptStream(InputStream in) {
		CipherInputStream cis = new CipherInputStream(in, getEncrypter());
		return cis;
	}

	public CipherOutputStream getDecryptStream(OutputStream out) {
		CipherOutputStream cos = new CipherOutputStream(out, getDecrypter());
		return cos;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Cipher getEncrypter() {
		return encrypter;
	}

	private void setEncrypter(Cipher encrypter) {
		this.encrypter = encrypter;
	}

	public Cipher getDecrypter() {
		return decrypter;
	}

	private void setDecrypter(Cipher decrypter) {
		this.decrypter = decrypter;
	}

}
