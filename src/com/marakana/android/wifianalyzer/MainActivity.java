package com.marakana.android.wifianalyzer;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	private static final String TAG = "WifiAnalyzer";

	private WifiManager wifiManager;
	private FrameLayout chartLayout;
	private XYMultipleSeriesRenderer renderer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// UI stuff
		setContentView(R.layout.activity_main);
		chartLayout = (FrameLayout) findViewById(R.id.chart);

		// Get WifiManager and make sure it's enabled
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
		}
	}

	private static final IntentFilter FILTER = new IntentFilter(
			WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

	@Override
	public void onStart() {
		super.onStart();
		// Register the scan receiver
		registerReceiver(scanReceiver, FILTER);
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unregister the receiver
		unregisterReceiver(scanReceiver);
	}

	/** Handles refresh button, initiates the scanning process. */
	public void onClickRefresh(View v) {
		wifiManager.startScan();
		Log.d(TAG, "onClickRefresh");
	}

	/** Gets called by the system once we have the scan results. */
	BroadcastReceiver scanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "scanReceiver received!");
			// Refreshes the chart once we have scan results
			View chartView = ChartFactory.getLineChartView(MainActivity.this,
					getDataset(), renderer);

			chartLayout.removeAllViews();
			chartLayout.addView(chartView);
		}
	};

	private XYMultipleSeriesDataset getDataset() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		renderer = getRenderer();
		List<ScanResult> results = wifiManager.getScanResults();

		XYSeries series = new XYSeries("Scan Results");

		int i = 1;
		for (ScanResult result : results) {
			i++;
			series.add(i, result.level);
			renderer.addXTextLabel(i, result.SSID);
		}
		dataset.addSeries(series);

		return dataset;
	}

	private XYMultipleSeriesRenderer getRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(20);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(20);
		renderer.setLegendTextSize(20);
		renderer.setPointSize(10f);
		renderer.setMargins(new int[] { 0, 0, 0, 0 });
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.WHITE);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);

		return renderer;
	}
}
