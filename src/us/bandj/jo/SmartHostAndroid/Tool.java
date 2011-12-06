package us.bandj.jo.SmartHostAndroid;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

public class Tool {

	private static final String TAG = "Smart Host for Android";
	public static final String PREFS_NAME = "Smart Host for Android";

	public static void fetchAndStoreAFile(String urlString, String path)
			throws IOException {
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		HttpGet get = new HttpGet(urlString);
		HttpResponse response = client.execute(get);
		InputStream s = response.getEntity().getContent();
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		byte[] buf = new byte[1024];
		int length;
		while ((length = s.read(buf)) != -1) {
			out.write(buf, 0, length);
		}
		s.close();
		out.close();
	}

	public static String getMD5(String path) throws NoSuchAlgorithmException,
			IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream in = new FileInputStream(path);
		try {
			in = new DigestInputStream(in, md);
			byte[] buf = new byte[1024];
			while (in.read(buf) != -1) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			String hex = Integer.toHexString(0xFF & digest[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static boolean runRootCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		return true;
	}

	public static void moveAFile(InputStream in, String to) throws IOException {
		int length;
		OutputStream out = new FileOutputStream(to);
		byte[] buf = new byte[1024];
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}
		out.close();
		Tool.runRootCommand("chmod 777 " + to);
	}

	public static void writeObject(Object o, String path) throws IOException {
		FileOutputStream out = new FileOutputStream(path);
		ObjectOutputStream _out = new ObjectOutputStream(out);
		_out.writeObject(o);
		out.close();
	}

	@SuppressWarnings("rawtypes")
	public static Object readObject(Class cls, String path) throws IOException,
			ClassNotFoundException {
		FileInputStream in = new FileInputStream(path);
		ObjectInputStream _in = new ObjectInputStream(in);
		Object o = _in.readObject();
		in.close();
		if (o.getClass() == cls) {
			return o;
		} else {
			throw new ClassNotFoundException("class mismatch.");
		}
	}

}
