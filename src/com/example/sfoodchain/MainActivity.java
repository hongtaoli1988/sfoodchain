package com.example.sfoodchain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.sfoodchain.utils.CurrentVersion;
import com.example.sfoodchain.utils.GetUpdateInfo;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	private static final String TAG = "Update";
	private String appName = "sFoodChain.apk";
	private String appVersion = "version.json";
	private int newVerCode = 0;
	private String newVerName = "";
	private ProgressDialog pBar;
	private String downPath = "http://120.25.156.253/";
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 食物链开始
		try {
			if (isNetworkAvailable(this) == false) {
				Log.i("cesi", "dddddddddddddddddddddddddd");
				return;
			} else {
				checkToUpdate();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 打开网页
	private void openUri() {
		Uri uri = Uri.parse("http://120.25.156.253/");
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		this.startActivity(intent);
	}

	// 检测是否有网络连接
	private static boolean isNetworkAvailable(Context context) {
		// TODO Auto-generated method stub
		try {

			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
			return (netWorkInfo != null && netWorkInfo.isAvailable());// 检测网络是否可用
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 检测是否需要更新版本
	private void checkToUpdate() throws NameNotFoundException {
		// TODO Auto-generated method stub
//		if (getServerVersion()) {
//			int currentCode = CurrentVersion.getVerCode(this);
//			if (newVerCode > currentCode) {// Current Version is old
//											// 弹出更新提示对话框
//				showUpdateDialog();
//			}
//			// 不需要更新时打开网页
//			else {
//				openUri();
//			}
//		}
		
		openUri();
	}

	// 从服务器获得版本比较
	private boolean getServerVersion() {
		// TODO Auto-generated method stub
		try {
			String newVerJSON = GetUpdateInfo.getUpdataVerJSON(downPath
					+ appVersion);
			JSONArray jsonArray = new JSONArray(newVerJSON);
			if (jsonArray.length() > 0) {
				JSONObject obj = jsonArray.getJSONObject(0);
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
					newVerName = obj.getString("verName");
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	// show Update Dialog
	private void showUpdateDialog() throws NameNotFoundException {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(CurrentVersion.getVerName(this));
		sb.append(" ");
		sb.append("版本号：");
		sb.append(CurrentVersion.getVerCode(this));
		sb.append("\n");
		sb.append("发现新版本：");
		sb.append(newVerName);
		sb.append(" ");
		sb.append("版本号：");
		sb.append(newVerCode);
		sb.append("\n");
		sb.append("是否更新？");
		Dialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("软件更新")
				.setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showProgressBar();// 更新当前版本
					}
				})
				.setNegativeButton("暂不更新",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								// openUri();
							}
						}).create();
		dialog.show();
	}

	protected void showProgressBar() {
		// TODO Auto-generated method stub
		pBar = new ProgressDialog(MainActivity.this);
		pBar.setTitle("正在下载");
		pBar.setMessage("请稍后...");
		pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		downAppFile(downPath + appName);
	}

	protected void downAppFile(final String url) {
		pBar.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					Log.isLoggable("DownTag", (int) length);
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is == null) {
						throw new RuntimeException("isStream is null");
					}
					File file = new File(
							Environment.getExternalStorageDirectory(), appName);
					fileOutputStream = new FileOutputStream(file);
					byte[] buf = new byte[1024];
					int ch = -1;
					do {
						ch = is.read(buf);
						if (ch <= 0)
							break;
						fileOutputStream.write(buf, 0, ch);
					} while (true);
					is.close();
					fileOutputStream.close();
					haveDownLoad();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// cancel progressBar and start new App
	protected void haveDownLoad() {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			public void run() {
				pBar.cancel();
				// 弹出警告框 提示是否安装新的版本
				Dialog installDialog = new AlertDialog.Builder(
						MainActivity.this)
						.setTitle("下载完成")
						.setMessage("是否安装新的应用")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										installNewApk();
										finish();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										finish();
										// 原生态android中不用加此方法
										// openUri();
									}
								}).create();
				installDialog.show();
			}
		});
	}

	// 安装新的应用
	protected void installNewApk() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), appName)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
