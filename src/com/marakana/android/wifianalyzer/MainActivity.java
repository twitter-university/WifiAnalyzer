package com.marakana.android.wifianalyzer;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
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
		} else {
			// Request a scan
			wifiManager.startScan();
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
			View chartView = ChartFactory.getBarChartView(MainActivity.this,
					getDataset(), renderer, BarChart.Type.STACKED);

			chartLayout.removeAllViews();
			chartLayout.addView(chartView);

			// Request a scan again
			wifiManager.startScan();
		}
	};

	private XYMultipleSeriesDataset getDataset() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		renderer = getRenderer();
		renderer.setShowGrid(true);
		renderer.setBarSpacing(0.20000000000000001D);
		renderer.setChartTitle("WiFi Scanner");
		renderer.setXTitle("WiFi Beacon");
		renderer.setYTitle("SNR");

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
		renderer.setLabelsTextSize(22);
		renderer.setLegendTextSize(20);
		renderer.setPointSize(10f);
		renderer.setXLabelsAngle(270);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		renderer.setBackgroundColor(Color.BLUE);
		SimpleSeriesRenderer r = new XYSeriesRenderer();
		renderer.setYAxisMax(-110);
		renderer.setYAxisMin(0);
		renderer.setXAxisMin(1);
		renderer.setXAxisMax(15);
		renderer.setShowAxes(true);
		renderer.setShowLegend(true);
		renderer.setShowGridX(true);
		renderer.setXLabels(15);
		renderer.setYLabels(15);

		renderer.setClickEnabled(false);
		renderer.setExternalZoomEnabled(false);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(false, false);
		r.setDisplayChartValues(true);
		r.setChartValuesTextSize(20);
		r.setColor(Color.BLUE);

		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.WHITE);

		return renderer;
	}
}
