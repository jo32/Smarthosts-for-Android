package us.bandj.jo.SmartHostAndroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class SmartHostAndroidActivity extends Activity
		implements
			OnClickListener {

	public final static int ABOUT_DIALOG_ID = 0;
	public final static int HELP_DIALOG_ID = 1;
	public final static int SHARE_DIALOG_ID = 10000001;

	public final static int SHOW_GETTING_URL_DIALOG = 2;
	public final static int HIDE_GETTING_URL_DIALOG = 3;
	public final static int LOAD_HOST_URL = 4;
	public final static int SET_STATUS_STRING = 5;
	public final static int SET_SERVER_STATUS_STRING = 6;
	public final static int UPDATE_HOST = 7;
	public final static int REVERT_HOST = 8;

	private Profile profile;

	private TextView status;
	private TextView serverStatus;
	private EditText hostsFileUrl;

	private String statusString;

	private Handler mainHandler;
	private Handler loadURLHandler;

	private Context context = null;

	ProgressDialog loadingProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	public void init() {
		Button change = (Button) this.findViewById(R.id.change_btn);
		change.setOnClickListener(this);
		Button check = (Button) this.findViewById(R.id.check_btn);
		check.setOnClickListener(this);
		Button revert = (Button) this.findViewById(R.id.revert_btn);
		revert.setOnClickListener(this);
		Button hostsFileUrlRevert = (Button) this
				.findViewById(R.id.hosts_file_source_revert_btn);
		hostsFileUrlRevert.setOnClickListener(this);
		EditText hostsFileUrl = (EditText) this
				.findViewById(R.id.hosts_file_source_et);
		this.hostsFileUrl = hostsFileUrl;
		hostsFileUrl.setOnClickListener(this);
		this.context = this;
		TextView status = (TextView) this.findViewById(R.id.status_tv);
		setStatus(status);

		TimeConsumingThread loadURLThread = new TimeConsumingThread();
		loadURLThread.start();

		while (loadURLHandler == null) {
		}

		mainHandler = new MainHandler();

		File file = new File(Config.APP_PATH + "hosts");

		if (!file.exists()) {
			AssetManager am = context.getAssets();
			InputStream in = null;
			try {
				File folder = new File(Config.APP_PATH);
				if (!folder.exists()) {
					folder.mkdir();
				}
				String[] files = {"cp.sh", "hosts", "hosts.empty", "rm.sh"};
				for (String _file : files) {
					in = am.open(_file);
					Tool.moveAFile(in, Config.APP_PATH + _file);
					in.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File profileFile = new File(context.getFilesDir() + "/profile");

		try {

			if (!profileFile.exists()) {
				Profile p = new Profile();
				Tool.writeObject(p, context.getFilesDir() + "/profile");
				setProfile(p);
			} else {
				Profile p = (Profile) Tool.readObject(Profile.class,
						context.getFilesDir() + "/profile");
				setProfile(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		initFromProfile();

		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	}
	public void initFromProfile() {
		getHostsFileUrl().setText(getProfile().getHostFileUrl());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about :
				showDialog(ABOUT_DIALOG_ID);
				return true;
			case R.id.help :
				showDialog(HELP_DIALOG_ID);
				return true;
			case R.id.share :
				this.sendShareActivity();
				return true;
			default :
				return false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Context myContext = this;
		Dialog dialog = new Dialog(myContext);
		switch (id) {
			case ABOUT_DIALOG_ID :
				dialog.setContentView(R.layout.about_dialog);
				dialog.setTitle(getResources().getString(R.string.about));
				break;
			case HELP_DIALOG_ID :
				dialog.setContentView(R.layout.help_dialog);
				dialog.setTitle(getResources().getString(R.string.help));
				break;
		}
		return dialog;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.change_btn :
				Message _msg = loadURLHandler.obtainMessage(UPDATE_HOST);
				loadURLHandler.sendMessage(_msg);
				break;
			case R.id.check_btn :
				Message msg = mainHandler
						.obtainMessage(SHOW_GETTING_URL_DIALOG);
				mainHandler.sendMessage(msg);
				break;
			case R.id.revert_btn :
				Message _msg2 = loadURLHandler.obtainMessage(REVERT_HOST);
				loadURLHandler.sendMessage(_msg2);
				break;
			case R.id.hosts_file_source_et :
				InputDialog id = new InputDialog(this, getResources()
						.getString(R.string.input_hint), null, getProfile()
						.getHostFileUrl(), InputDialog.HOST_FILE_URL_INPUT);
				id.show();
				break;
			case R.id.hosts_file_source_revert_btn :
				this.getHostsFileUrl().setText(
						getProfile().getDefaultHostFileUrl());
				this.getProfile().setHostFileUrl(
						this.getProfile().getDefaultHostFileUrl());
				break;
		}
	}

	@Override
	protected void onPause() {
		try {
			Tool.writeObject(getProfile(), context.getFilesDir() + "/profile");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	class TimeConsumingThread extends Thread {

		@Override
		public void run() {
			Looper.prepare();

			loadURLHandler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case LOAD_HOST_URL :
							try {
								String oldPath = context.getFilesDir()
										+ "/hosts";
								File oldFile = new File(oldPath);
								String newPath = context.getFilesDir()
										+ "/hosts.new";
								File newFile = new File(newPath);
								Tool.fetchAndStoreAFile(getProfile()
										.getHostFileUrl(), newPath);
								String statusString = getResources().getString(
										R.string.new_hosts_file);
								if (oldFile.exists()) {
									String md5_1 = Tool.getMD5(oldPath);
									String md5_2 = Tool.getMD5(newPath);
									if (md5_1.equals(md5_2)) {
										statusString = getResources()
												.getString(
														R.string.no_new_hosts_file);
										newFile.delete();
									} else {
										oldFile.delete();
										newFile.renameTo(oldFile);
									}
								} else {
									newFile.renameTo(oldFile);
								}
								setStatusString(statusString);
								Message _msg = mainHandler
										.obtainMessage(SET_STATUS_STRING);
								mainHandler.sendMessage(_msg);
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								Message __msg = mainHandler
										.obtainMessage(HIDE_GETTING_URL_DIALOG);
								mainHandler.sendMessage(__msg);
							}
							break;
						case UPDATE_HOST :
							Tool.runRootCommand(Config.APP_PATH + "cp.sh");
							setStatusString(getResources().getString(
									R.string.replaced_with_new_hosts_file));
							Message _msg2 = mainHandler
									.obtainMessage(SET_STATUS_STRING);
							mainHandler.sendMessage(_msg2);
							break;
						case REVERT_HOST :
							Tool.runRootCommand(Config.APP_PATH + "rm.sh");
							setStatusString("reverted the host file to be empty.");
							Message _msg3 = mainHandler
									.obtainMessage(SET_STATUS_STRING);
							mainHandler.sendMessage(_msg3);
							break;
					}
					super.handleMessage(msg);
				}

			};

			Looper.loop();
		}
	}

	class MainHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_GETTING_URL_DIALOG :
					if (loadingProgressDialog == null) {
						loadingProgressDialog = ProgressDialog.show(
								context,
								"",
								getResources().getString(
										R.string.loading_host_file));
					} else {
						loadingProgressDialog.show();
					}
					Message _msg = loadURLHandler.obtainMessage(LOAD_HOST_URL);
					loadURLHandler.sendMessage(_msg);
					break;
				case HIDE_GETTING_URL_DIALOG :
					loadingProgressDialog.hide();
					break;
				case SET_STATUS_STRING :
					String status = getStatusString();
					getStatus().setText(status);
					break;
			}
		}
	}

	private void sendShareActivity() {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT,
				getResources().getString(R.string.share_text));
		startActivity(Intent.createChooser(share,
				getResources().getString(R.string.share)));
	}

	public TextView getStatus() {
		return status;
	}

	public void setStatus(TextView status) {
		this.status = status;
	}

	public TextView getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(TextView serverStatus) {
		this.serverStatus = serverStatus;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public EditText getHostsFileUrl() {
		return hostsFileUrl;
	}

	public void setHostsFileUrl(EditText hostsFileUrl) {
		this.hostsFileUrl = hostsFileUrl;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	public class InputDialog implements DialogInterface.OnClickListener {

		private AlertDialog.Builder alert;
		private EditText input;
		private String text;
		private Context c;
		private int whichInput;
		private String oldMessage;

		public final static int HOST_FILE_URL_INPUT = 0;

		public InputDialog(Context c, String title, String msg,
				String oldMessage, int whichInput) {
			this.c = c;
			this.whichInput = whichInput;
			this.oldMessage = oldMessage;
			alert = new AlertDialog.Builder(c);
			alert.setTitle(title);
			alert.setMessage(msg);
			input = new EditText(c);
			alert.setView(input);
			input.setText(oldMessage);

			alert.setPositiveButton(c.getResources()
					.getString(R.string.confirm), this);
			alert.setNegativeButton(
					c.getResources().getString(R.string.cancel), this);

		}

		@Override
		public void onClick(DialogInterface di, int which) {
			switch (which) {
				case DialogInterface.BUTTON1 : {
					this.setText(input.getText().toString());
					break;
				}
				case DialogInterface.BUTTON2 : {
					this.setText(oldMessage);
					break;
				}
			}
			switch (whichInput) {
				case HOST_FILE_URL_INPUT :
					EditText ed = (EditText) ((Activity) c)
							.findViewById(R.id.hosts_file_source_et);
					ed.setText(getText());
					getProfile().setHostFileUrl(text);
					break;
			}
		}
		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void show() {
			this.alert.show();
		}

	}

}