package com.example.testweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final boolean DEBUG = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.replace(R.id.container, new PlaceholderFragment())
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	class PlaceholderFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			TextView txt = ((TextView) MainActivity.this
					.findViewById(R.id.textView1));
			String str = "";
			switch (msg.what) {
			case 1:
				str = ((JSONObject) msg.obj).toString();
				break;
			case 2:
				str = (String) msg.obj;
				break;
			}
			txt.setText(str);
		}

	};

	public void getWeatcer(View v) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mHandler.sendMessage(mHandler.obtainMessage(2, "load..."));
					String location = getCity();
					mHandler.sendMessage(mHandler.obtainMessage(
							2,
							"load... " + location + " "
									+ URLEncoder.encode(location, "utf-8")));

					String weatherReqUrl = "http://api.map.baidu.com/telematics/v3/weather?location="
							+ URLEncoder.encode(location, "utf-8")
							+ "&output=json&ak=FMvGjV8o6QMuxPum9ph2VdEK";
					String object = sendRequset(weatherReqUrl);
					mHandler.sendMessage(mHandler.obtainMessage(2, object));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("tt", "Exception: " + e);
					mHandler.sendMessage(mHandler.obtainMessage(2, "error: "
							+ e));
				}

			}

		}).start();
	}

	private String getNetIp() {
		String ipLine = "";
		String strber = sendRequset("http://ip168.com/json.do?view=myipaddress");
		Pattern pattern = Pattern
				.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
		Matcher matcher = pattern.matcher(strber.toString());
		if (matcher.find()) {
			ipLine = matcher.group();
		}
		return ipLine;
	}

	private String getCity() {
		String requsetResult = sendRequset("http://ip168.com/json.do?view=myipaddress");
		Pattern pattern = Pattern
				.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
		Matcher matcher = pattern.matcher(requsetResult.toString());
		String cityStr = "";
		if (matcher.find()) {
			String ipLine = "";
			ipLine = matcher.group();
			int startPosition = requsetResult.indexOf(ipLine) + ipLine.length()
					+ 5;
			int endPosition = requsetResult.indexOf("</center>") - 3;
			cityStr = requsetResult.substring(startPosition, endPosition);
			if (cityStr.contains("省")) {
				cityStr = cityStr.substring(cityStr.indexOf("省") + 1,
						cityStr.length());
			}
		}
		return cityStr;
	}

	private String sendRequset(String url) {
		URL infoUrl = null;
		InputStream inStream = null;
		HttpURLConnection httpConnection = null;
		StringBuilder strber = new StringBuilder();
		try {
			infoUrl = new URL(url);
			URLConnection connection = infoUrl.openConnection();
			httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inStream = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inStream, "utf-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					strber.append(line);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
				httpConnection.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return strber.toString();
	}
}
