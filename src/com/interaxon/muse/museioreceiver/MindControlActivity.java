package com.interaxon.muse.museioreceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

@SuppressWarnings("unused")
public class MindControlActivity extends Activity implements
		OnClickListener, OnItemSelectedListener {
	
	static {
        // Loads the muse native library.
        System.loadLibrary("muse_android");
    }
	
	// Muse variables
	private Muse _muse = null;
    private ConnectionListener _connectionListener = null;
    private DataListener _dataListener = null;
    private boolean _bDataTransmission = true;
    private ConnectionState _museConnectionState = ConnectionState.UNKNOWN;
	
	// Main menu views
	private RelativeLayout _rl_main_menu = null;
	private TextView _tv_main_muse_selector = null;
	private Spinner _sp_main_muse_spinner = null;
	private Button _btn_main_muse_refresh = null;
	private Button _btn_main_muse_connect = null;
	private Button _btn_main_muse_disconnect = null;
	private Button _btn_main_relax_data = null;
	private Button _btn_main_focus_data = null;
	private Button _btn_main_mind_control = null;
	private TextView _tv_main_current_k_value = null;
	private Spinner _sp_main_k_value = null;
	private Button _btn_main_cross_validation = null;
	
	// Main menu - Horseshoe views
	private TextView _tv_main_connection_quality = null;
	private TextView _tv_main_horseshoe_marker = null;
	private TextView _tv_main_horseshoe_tp9 = null;
	private TextView _tv_main_horseshoe_fp1 = null;
	private TextView _tv_main_horseshoe_fp2 = null;
	private TextView _tv_main_horseshoe_tp10 = null;
	
	// Relax menu views
	private RelativeLayout _rl_relax_menu = null;
	private TextView _tv_relax_inst = null;
	private Button _btn_relax_start = null;
	private TextView _tv_relax_timer = null;
	private Button _btn_relax_end = null;
	
	// Focus menu views
	private RelativeLayout _rl_focus_menu = null;
	private TextView _tv_focus_inst = null;
	private ImageView _iv_focus_wally = null;
	private Button _btn_focus_start = null;
	private TextView _tv_focus_timer = null;
	private ImageView _iv_focus_wallyGameImage = null;
	private Button _btn_focus_wallyGameSoln = null;
	private Button _btn_focus_end = null;
	
	private Bitmap _bWallyGameImage = null;
	private Bitmap _bWallyGameImageSoln = null;
	
	// Cross-validation menu views
	private RelativeLayout _rl_cross_validation = null;
	private TextView _tv_cv_k_value = null;
	private TextView _tv_cv_results = null;
	private Button _btn_cv_end = null;
	private ProgressDialog _progDialog_crossValidation = null;
	
	// Mind Control menu views
	private RelativeLayout _rl_mindControl_menu = null;
	private RadioGroup _rg_mindControl_options = null;
	private Button _btn_mindControl_start = null;
	private Button _btn_mindControl_end = null;
	
	// Mind Control Activity 1
	private RelativeLayout _rl_mindControlOpt1 = null;
	private TextView _tv_mindControlOpt1_status = null;
	private Button _btn_mindControlOpt1_end = null;
	private TextView _tv_mindControlOpt1_focus = null;
	private TextView _tv_mindControlOpt1_relax = null;
	private ProgressBar _pb_mindControlOpt1_focusStatus = null;
	private ProgressBar _pb_mindControlOpt1_relaxStatus = null;
	
	// Mind Control Activity 2
	private RelativeLayout _rl_mindControlOpt2 = null;
	private TextView _tv_mindControlOpt2_status = null;
	private Button _btn_mindControlOpt2_end = null;
	private TextView _tv_mindControlOpt2_focus = null;
	private TextView _tv_mindControlOpt2_relax = null;
	private ProgressBar _pb_mindControlOpt2_focusStatus = null;
	private ProgressBar _pb_mindControlOpt2_relaxStatus = null;
	private TextView _tv_mindControlOpt2_volume = null;
	private MediaPlayer _mindControlOpt2_mp = null;
	private AudioManager _mindControlOpt2_am = null;
	private int _mindControlOpt2_currVol = 0;
	private int _mindControlOpt2_maxVol = 0;
	
	// Countdown timer
	private MoreAccurateTimer cdTimer = null;
	private static final int CD_DURATION = 30;
	
	// Boolean variables to control EEG recordings
	private boolean _bRecordingRelax = false;
	private boolean _bRecordingFocus = false;
	
	// ArrayLists for storing incoming raw EEG data
	private ArrayList<ArrayList<Double>> _arr_TP9 = null;
	private ArrayList<ArrayList<Double>> _arr_FP1 = null;
	private ArrayList<ArrayList<Double>> _arr_FP2 = null;
	private ArrayList<ArrayList<Double>> _arr_TP10 = null;
	
	// Constants for Window Size, Shift Amount, Number of buffers
	private static final int WINDOW_SIZE = 256;
	private static final int SHIFT_AMT = 128;
	private static final int BUFFERS = 3;
	
	// Window for processing raw EEG data
	private ArrayList<ArrayList<Double>> _eeg_Window = null;
	private int _currBuffer = 0;
	
	// For storing data
	private static final int MAX_QUEUE_SIZE = 50;
	private ArrayList<Double [][]> _tempFeatures = null;
	private ArrayDeque<FeatureVector> _relaxFv = null;
	private ArrayDeque<FeatureVector> _focusFv = null;
	private static final int FOCUS_CLASS_LABEL = 1;
	private static final int RELAX_CLASS_LABEL = 2;
	
	// For testing mind control
	private boolean _bMindControl = false;
	private FeatureVector [] _combinedFvArr = null;
	private double [] _stdDevArr = null;
	private static int _kValue = 9;
	private int _activeMindControlActivity = 0;
	
	// For doing 10-fold cross validation on accuracy of k-nn algorithm
	
	
	public enum DataType
	{
		RelaxData,
		FocusData,
		TestData
	}
	
	public MindControlActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                                new WeakReference<Activity>(this);
        _connectionListener = new ConnectionListener(weakActivity);
        _dataListener = new DataListener(weakActivity);
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.mind_control_main);
		
		// Find reference to all the on-screen views
		initMainMenuViews();
		initRelaxMenuViews();
		initFocusMenuViews();
		initCvMenuViews();
		initMindControlMenuViews();
		initOption_1_Views();
		initOption_2_Views();
		
		_bWallyGameImage = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.cropped_waldo_002);
		_bWallyGameImageSoln = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.cropped_waldo_002_soln);
		
		_arr_TP9 = new ArrayList<ArrayList<Double>>(BUFFERS);
		_arr_FP1 = new ArrayList<ArrayList<Double>>(BUFFERS);
		_arr_FP2 = new ArrayList<ArrayList<Double>>(BUFFERS);
		_arr_TP10 = new ArrayList<ArrayList<Double>>(BUFFERS);
		for(int i = 0; i < 3; i++) {
			_arr_TP9.add(new ArrayList<Double>());
			_arr_FP1.add(new ArrayList<Double>());
			_arr_FP2.add(new ArrayList<Double>());
			_arr_TP10.add(new ArrayList<Double>());
		}
		
		// Stores the ArrayLists for each of the 4 sampling points
		_eeg_Window = new ArrayList<ArrayList<Double>>(4);
		for(int i = 0; i < 4; i++) {
			_eeg_Window.add(new ArrayList<Double>());
		}
		
		// For storing data
		_tempFeatures = new ArrayList<Double [][]>();
		_relaxFv = new ArrayDeque<FeatureVector>();
		_focusFv = new ArrayDeque<FeatureVector>();
		
		loadFvData();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// Enable data transmission for Muse device
		_bDataTransmission = true;
		if (_muse != null) {
			_muse.enableDataTransmission(_bDataTransmission);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		
		// Disable data transmission for Muse device
		_bDataTransmission = false;
        if (_muse != null) {
            _muse.enableDataTransmission(_bDataTransmission);
        }
		
		// Stop any ongoing countdown timer
		if(cdTimer != null)
			cdTimer.cancel();
	}
	
	private void initMainMenuViews() {
		
		// Get reference to main menu views
		_rl_main_menu = (RelativeLayout) MindControlActivity.this
				.findViewById(R.id.rl_main_menu);
		_tv_main_muse_selector = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_muse_selector);
		_sp_main_muse_spinner = (Spinner) MindControlActivity.this
				.findViewById(R.id.sp_main_muse_spinner);
		_btn_main_muse_refresh = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_muse_refresh);
		_btn_main_muse_connect = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_muse_connect);
		_btn_main_muse_disconnect = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_muse_disconnect);
		_btn_main_relax_data = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_relax_data);
		_btn_main_focus_data = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_focus_data);
		_btn_main_mind_control = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_mind_control);
		
		// Get reference to main menu 'horseshoe' views
		_tv_main_connection_quality = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_connection_quality);
		_tv_main_horseshoe_marker = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_horseshoe_marker);
		_tv_main_horseshoe_tp9 = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_horseshoe_tp9);
		_tv_main_horseshoe_fp1 = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_horseshoe_fp1);
		_tv_main_horseshoe_fp2 = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_horseshoe_fp2);
		_tv_main_horseshoe_tp10 = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_horseshoe_tp10);
		
		// Get reference to the k value & cross-validation views
		_tv_main_current_k_value = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_main_current_k_value);
		
		_sp_main_k_value = (Spinner) MindControlActivity.this
				.findViewById(R.id.sp_main_k_value);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.k_value_array, R.layout.spinner_dropdown_layout);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		_sp_main_k_value.setAdapter(adapter);
		
		_btn_main_cross_validation = (Button) MindControlActivity.this
				.findViewById(R.id.btn_main_cross_validation);
		
		// Add onClickListeners to main menu buttons
		_btn_main_muse_refresh.setOnClickListener(this);
		_btn_main_muse_connect.setOnClickListener(this);
		_btn_main_muse_disconnect.setOnClickListener(this);
		_btn_main_relax_data.setOnClickListener(this);
		_btn_main_focus_data.setOnClickListener(this);
		_btn_main_mind_control.setOnClickListener(this);
		_btn_main_cross_validation.setOnClickListener(this);
		
		// Add onItemSelectedListener for the Spinner
		_sp_main_k_value.setOnItemSelectedListener(this);
	}
	
	private void initRelaxMenuViews() {
		
		// Get reference to relax menu views
		_rl_relax_menu = (RelativeLayout) MindControlActivity.this
			.findViewById(R.id.rl_relax_menu);
		_tv_relax_inst = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_relax_inst);
		_btn_relax_start = (Button) MindControlActivity.this
				.findViewById(R.id.btn_relax_start);
		_tv_relax_timer = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_relax_timer);
		_btn_relax_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_relax_end);
		
		// Add onClickListeners to relax menu buttons
		_btn_relax_start.setOnClickListener(this);
		_btn_relax_end.setOnClickListener(this);
	}
	
	private void initFocusMenuViews() {
		
		// Get reference to focus menu views
		_rl_focus_menu = (RelativeLayout) MindControlActivity.this
				.findViewById(R.id.rl_focus_menu);
		
		_tv_focus_inst = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_focus_inst);
		_iv_focus_wally = (ImageView) MindControlActivity.this
				.findViewById(R.id.iv_focus_wally);
		_btn_focus_start = (Button) MindControlActivity.this
				.findViewById(R.id.btn_focus_start);
		
		_tv_focus_timer = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_focus_timer);
		_iv_focus_wallyGameImage = (ImageView) MindControlActivity.this
				.findViewById(R.id.iv_focus_wallyGameImage);
		_btn_focus_wallyGameSoln = (Button) MindControlActivity.this
				.findViewById(R.id.btn_focus_wallyGameSoln);
		_btn_focus_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_focus_end);

		// Add onClickListeners to focus menu buttons
		_btn_focus_start.setOnClickListener(this);
		_btn_focus_wallyGameSoln.setOnClickListener(this);
		_btn_focus_end.setOnClickListener(this);
	}
	
	private void initCvMenuViews() {
		
		// Get reference to cross-validation menu views
		_rl_cross_validation = (RelativeLayout) MindControlActivity.this
				.findViewById(R.id.rl_cross_validation);
		_tv_cv_k_value = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_cv_k_value);
		_tv_cv_results = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_cv_results);
		_btn_cv_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_cv_end);
		
		// Add onClickListeners to cross-validation menu buttons
		_btn_cv_end.setOnClickListener(this);
	}
	
	private void initMindControlMenuViews() {
		
		// Get reference to mind control menu views
		_rl_mindControl_menu = (RelativeLayout) MindControlActivity.this
			.findViewById(R.id.rl_mindControl_menu);
		_rg_mindControl_options = (RadioGroup) MindControlActivity.this
				.findViewById(R.id.rg_mindControl_options);
		_btn_mindControl_start = (Button) MindControlActivity.this
				.findViewById(R.id.btn_mindControl_start);
		_btn_mindControl_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_mindControl_end);
		
		// Add onClickListeners to mind control menu buttons
		_btn_mindControl_start.setOnClickListener(this);
		_btn_mindControl_end.setOnClickListener(this);
	}
	
	private void initOption_1_Views() {
		
		// Get reference to mind control option 1 views
		_rl_mindControlOpt1 = (RelativeLayout) MindControlActivity.this
			.findViewById(R.id.rl_mindControlOpt1);
		_tv_mindControlOpt1_status = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt1_status);
		_btn_mindControlOpt1_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_mindControlOpt1_end);
		_tv_mindControlOpt1_focus = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt1_focus);
		_tv_mindControlOpt1_relax = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt1_relax);
		_pb_mindControlOpt1_focusStatus = (ProgressBar) MindControlActivity.this
				.findViewById(R.id.pb_mindControlOpt1_focusStatus);
		_pb_mindControlOpt1_relaxStatus = (ProgressBar) MindControlActivity.this
				.findViewById(R.id.pb_mindControlOpt1_relaxStatus);
		
		// Add onClickListeners to mind control option 1 buttons
		_btn_mindControlOpt1_end.setOnClickListener(this);
	}
	
	private void initOption_2_Views() {
		
		// Get reference to mind control option 2 views
		_rl_mindControlOpt2 = (RelativeLayout) MindControlActivity.this
			.findViewById(R.id.rl_mindControlOpt2);
		_tv_mindControlOpt2_status = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt2_status);
		_btn_mindControlOpt2_end = (Button) MindControlActivity.this
				.findViewById(R.id.btn_mindControlOpt2_end);
		_tv_mindControlOpt2_focus = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt2_focus);
		_tv_mindControlOpt2_relax = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt2_relax);
		_pb_mindControlOpt2_focusStatus = (ProgressBar) MindControlActivity.this
				.findViewById(R.id.pb_mindControlOpt2_focusStatus);
		_pb_mindControlOpt2_relaxStatus = (ProgressBar) MindControlActivity.this
				.findViewById(R.id.pb_mindControlOpt2_relaxStatus);
		_tv_mindControlOpt2_volume = (TextView) MindControlActivity.this
				.findViewById(R.id.tv_mindControlOpt2_volume);
		
		// Add onClickListeners to mind control option 2 buttons
		_btn_mindControlOpt2_end.setOnClickListener(this);
	}
	
	private void toggleMainMenu(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_main_menu.setEnabled(bActive);
		_rl_main_menu.setVisibility(visibility);
		
		_tv_main_muse_selector.setVisibility(visibility);
		_sp_main_muse_spinner.setVisibility(visibility);
		_btn_main_muse_refresh.setVisibility(visibility);
		_btn_main_muse_connect.setVisibility(visibility);
		_btn_main_muse_disconnect.setVisibility(visibility);
		_btn_main_relax_data.setVisibility(visibility);
		_btn_main_focus_data.setVisibility(visibility);
		_btn_main_mind_control.setVisibility(visibility);
		
		// Visibility for horseshoe views
		_tv_main_connection_quality.setVisibility(visibility);
		_tv_main_horseshoe_marker.setVisibility(visibility);
		_tv_main_horseshoe_tp9.setVisibility(visibility);
		_tv_main_horseshoe_fp1.setVisibility(visibility);
		_tv_main_horseshoe_fp2.setVisibility(visibility);
		_tv_main_horseshoe_tp10.setVisibility(visibility);
		
		// Visiblity for k-value & cross-validation views
		_tv_main_current_k_value.setVisibility(visibility);
		_sp_main_k_value.setVisibility(visibility);
		_btn_main_cross_validation.setVisibility(visibility);
	}
	
	private void toggleRelaxMenu(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_relax_menu.setEnabled(bActive);
		_rl_relax_menu.setVisibility(visibility);
		
		_tv_relax_inst.setVisibility(visibility);
		_btn_relax_start.setVisibility(visibility);
		
		_tv_relax_timer.setVisibility(View.INVISIBLE);
		_btn_relax_end.setVisibility(View.INVISIBLE);
	}
	
	private void toggleFocusMenu(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_focus_menu.setEnabled(bActive);
		_rl_focus_menu.setVisibility(visibility);
		
		_tv_focus_inst.setVisibility(visibility);
		_iv_focus_wally.setVisibility(visibility);
		_btn_focus_start.setVisibility(visibility);
		
		_tv_focus_timer.setVisibility(View.INVISIBLE);
		_iv_focus_wallyGameImage.setVisibility(View.INVISIBLE);
		_btn_focus_wallyGameSoln.setVisibility(View.INVISIBLE);
		_btn_focus_end.setVisibility(View.INVISIBLE);
	}
	
	private void toggleCvMenu(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_cross_validation.setEnabled(bActive);
		_rl_cross_validation.setVisibility(visibility);
		
		_tv_cv_k_value.setVisibility(visibility);
		_tv_cv_results.setVisibility(visibility);
		_btn_cv_end.setVisibility(visibility);
	}
	
	private void toggleMindControlMenu(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_mindControl_menu.setEnabled(bActive);
		_rl_mindControl_menu.setVisibility(visibility);
		
		_rg_mindControl_options.setVisibility(visibility);
		_btn_mindControl_start.setVisibility(visibility);
		_btn_mindControl_end.setVisibility(visibility);
	}
	
	private void toggleOption_1_Views(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_mindControlOpt1.setEnabled(bActive);
		_rl_mindControlOpt1.setVisibility(visibility);
		
		_tv_mindControlOpt1_status.setVisibility(visibility);
		_btn_mindControlOpt1_end.setVisibility(visibility);
		_tv_mindControlOpt1_focus.setVisibility(visibility);
		_tv_mindControlOpt1_relax.setVisibility(visibility);
		_pb_mindControlOpt1_focusStatus.setVisibility(visibility);
		_pb_mindControlOpt1_relaxStatus.setVisibility(visibility);
		
		_pb_mindControlOpt1_focusStatus.setMax(_kValue);
		_pb_mindControlOpt1_focusStatus.setProgress(0);
		
		_pb_mindControlOpt1_relaxStatus.setMax(_kValue);
		_pb_mindControlOpt1_relaxStatus.setProgress(0);
	}
	
	private void toggleOption_2_Views(boolean bActive) {
		
		int visibility = bActive ? View.VISIBLE : View.INVISIBLE;
		
		_rl_mindControlOpt2.setEnabled(bActive);
		_rl_mindControlOpt2.setVisibility(visibility);
		
		_tv_mindControlOpt2_status.setVisibility(visibility);
		_btn_mindControlOpt2_end.setVisibility(visibility);
		_tv_mindControlOpt2_focus.setVisibility(visibility);
		_tv_mindControlOpt2_relax.setVisibility(visibility);
		_pb_mindControlOpt2_focusStatus.setVisibility(visibility);
		_pb_mindControlOpt2_relaxStatus.setVisibility(visibility);
		_tv_mindControlOpt2_volume.setVisibility(visibility);
		
		_pb_mindControlOpt2_focusStatus.setMax(_kValue);
		_pb_mindControlOpt2_focusStatus.setProgress(0);
		
		_pb_mindControlOpt2_relaxStatus.setMax(_kValue);
		_pb_mindControlOpt2_relaxStatus.setProgress(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btn_main_muse_refresh:
			Toast.makeText(getApplicationContext(),
					"Refreshing list of paired Muse devices..", Toast.LENGTH_SHORT)
					.show();
			MuseManager.refreshPairedMuses();
			List<Muse> pairedMuses = MuseManager.getPairedMuses();
			List<String> spinnerItems = new ArrayList<String>();
			for (Muse m : pairedMuses) {
				String dev_id = m.getName() + "-" + m.getMacAddress();
				Log.i("Muse Headband", dev_id);
				spinnerItems.add(dev_id);
			}
			
			// Temporary
			spinnerItems.add("Do Not Use 1 - 00:11:22:33:44:55");
			spinnerItems.add("Do Not Use 2 - 00:11:22:33:44:66");
			spinnerItems.add("Do Not Use 3 - 00:11:22:33:44:77");
			
			ArrayAdapter<String> adapterArray = new ArrayAdapter<String>(this,
					R.layout.spinner_dropdown_layout, spinnerItems);
			_sp_main_muse_spinner.setAdapter(adapterArray);
			break;
		
		case R.id.btn_main_muse_connect:
			List<Muse> pairedMusesConn = MuseManager.getPairedMuses();
			if (pairedMusesConn.size() < 1
					|| _sp_main_muse_spinner.getAdapter().getCount() < 1) {
				Toast.makeText(getApplicationContext(),
						"No Muse device to connect to!", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Connecting to Muse device..", Toast.LENGTH_SHORT)
						.show();
				_muse = pairedMusesConn.get(_sp_main_muse_spinner
						.getSelectedItemPosition());
				
				// Set up connection
				_muse.registerConnectionListener(_connectionListener);
				
				// Set up listeners for different types of data
				_muse.registerDataListener(_dataListener,
						MuseDataPacketType.EEG);
				_muse.registerDataListener(_dataListener,
						MuseDataPacketType.ARTIFACTS);
				_muse.registerDataListener(_dataListener,
						MuseDataPacketType.HORSESHOE);
				
				// Other muse functions
				_muse.setPreset(MusePreset.PRESET_14);
				_muse.enableDataTransmission(_bDataTransmission);
				_muse.runAsynchronously();
			}
			break;
			
		case R.id.btn_main_muse_disconnect:
			if (_muse != null) {
                _muse.disconnect(true);
            }
			break;

		case R.id.btn_main_relax_data:
			
			// Check for connected Muse device
			if (_museConnectionState == ConnectionState.CONNECTED) {
				// Hide main menu and show relax menu
				toggleMainMenu(false);
				toggleRelaxMenu(true);
			} else {
				Toast.makeText(getApplicationContext(),
						"Connect to a paired Muse device to use this feature",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btn_main_focus_data:
			
			// Check for connected Muse device
			if (_museConnectionState == ConnectionState.CONNECTED) {
				// Hide main menu and show focus menu
				toggleMainMenu(false);
				toggleFocusMenu(true);
			} else {
				Toast.makeText(getApplicationContext(),
						"Connect to a paired Muse device to use this feature",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btn_main_mind_control:
			
			// Check for connected Muse device
			if (_museConnectionState == ConnectionState.CONNECTED) {
				
				// Check for collected relax & focus data
				if(_relaxFv.isEmpty() || _focusFv.isEmpty())
				{
					Toast.makeText(getApplicationContext(),
							"Use this application to collect relax & focus data before"
							+ " trying to inititate mind control!",
							Toast.LENGTH_SHORT).show();
				}
				else
				{
					// Initialize training vectors
					initTrainingData();
					
					// Do whatever else is necessary, e.g.
					toggleMainMenu(false);
					toggleMindControlMenu(true);
					
					/*clearWindow();
					clearAllBuffers();
					_currBuffer = 0;	
					_bMindControl = true;*/
					 
				}			
				
			} else {
				Toast.makeText(getApplicationContext(),
						"Connect to a paired Muse device to use this feature",
						Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.btn_main_cross_validation:
			
			// Hide main menu and show cross-validation menu
			toggleMainMenu(false);
			toggleCvMenu(true);
			
			new CrossValidationTask().execute();
			
			break;
			
		case R.id.btn_cv_end:
			
			// Hide cross-validation menu and show main menu
			toggleCvMenu(false);
			toggleMainMenu(true);
			
			break;
			
		case R.id.btn_relax_start:
			
			// Hide instructions and start button
			_tv_relax_inst.setVisibility(View.INVISIBLE);
			_btn_relax_start.setVisibility(View.INVISIBLE);
			
			// Show countdown timer
			_tv_relax_timer.setVisibility(View.VISIBLE);
			
			// Start a timer
			if(cdTimer != null)
				cdTimer.cancel();
			cdTimer = new MoreAccurateTimer((CD_DURATION * 1000), 1000) {

				public void onTick(long millisUntilFinished) {
					_tv_relax_timer.setText("Seconds remaining:\n" 
							+ (millisUntilFinished + 50) / 1000);
				}

				public void onFinish() {
					_tv_relax_timer.setText("Done!");
					_btn_relax_end.setVisibility(View.VISIBLE);

					_bRecordingRelax = false;
					
					playAlarm();
				}
			};
			cdTimer.start();
			_tv_relax_timer.setText("Seconds remaining:\n"
					+ Integer.toString(CD_DURATION));
			
			// Initialize variables used for data logging
			clearWindow();
			clearAllBuffers();
			_currBuffer = 0;	
			_bRecordingRelax = true;
			
			break;
			
		case R.id.btn_relax_end:
			// Save the feature vectors to file
			saveRelaxData();
			
			// Hide relax menu and show main menu
			toggleRelaxMenu(false);
			toggleMainMenu(true);
			break;
			
		case R.id.btn_focus_start:
			// Hide instructions, wally image and start button
			_tv_focus_inst.setVisibility(View.INVISIBLE);
			_iv_focus_wally.setVisibility(View.INVISIBLE);
			_btn_focus_start.setVisibility(View.INVISIBLE);
			
			// Show countdown timer
			_tv_focus_timer.setVisibility(View.VISIBLE);
			
			// Start a timer
			if(cdTimer != null)
				cdTimer.cancel();
			cdTimer = new MoreAccurateTimer((CD_DURATION * 1000), 1000) {

				public void onTick(long millisUntilFinished) {
					_tv_focus_timer.setText("" + (millisUntilFinished + 50)
							/ 1000);
				}

				public void onFinish() {
					
					// Set timer time to 0
					_tv_focus_timer.setText("0");
					
					// Disable solution button
					_btn_focus_wallyGameSoln.setVisibility(View.INVISIBLE);
					
					// Enable user to go back to main menu to try again
					_btn_focus_end.setVisibility(View.VISIBLE);
					
					_bRecordingFocus = false;
					playAlarm();
				}
			};
			cdTimer.start();
			_tv_focus_timer.setText(Integer.toString(CD_DURATION));
			
			// Initialize variables used for data logging
			clearWindow();
			clearAllBuffers();
			_currBuffer = 0;
			_bRecordingFocus = true;
			
			// Reset wally game
			_iv_focus_wallyGameImage.setVisibility(View.VISIBLE);
			_iv_focus_wallyGameImage.setImageBitmap(_bWallyGameImage);
			_btn_focus_wallyGameSoln.setVisibility(View.VISIBLE);
			break;
			
		case R.id.btn_focus_wallyGameSoln:
			
			// Stop ongoing countdown timer
			if(cdTimer != null)
			{
				cdTimer.cancel(); // Does not invoke onFinish()
			}
			
			// Found solution, stop the recording
			_bRecordingFocus = false;
			
			// Show solution for wally game
			_iv_focus_wallyGameImage.setImageBitmap(_bWallyGameImageSoln);
			
			// Disable solution button
			_btn_focus_wallyGameSoln.setVisibility(View.INVISIBLE);
			
			// Show main menu button
			_btn_focus_end.setVisibility(View.VISIBLE);
			break;
			
		case R.id.btn_focus_end:
			saveFocusData();
			
			// Hide relax menu and show main menu
			toggleFocusMenu(false);
			toggleMainMenu(true);
			break;
			
		case R.id.btn_mindControl_start:
			// Hide mind control menu
			toggleMindControlMenu(false);
			
			// Find out which option is selected
			switch(_rg_mindControl_options.getCheckedRadioButtonId())
			{
			case R.id.rb_mindControl_opt1:
				_activeMindControlActivity = 1;
				toggleOption_1_Views(true);
				break;
				
			case R.id.rb_mindControl_opt2:
				_activeMindControlActivity = 2;
				toggleOption_2_Views(true);
				
				playMusic();
				_mindControlOpt2_am = (AudioManager)
						getApplicationContext().getSystemService(
								Context.AUDIO_SERVICE);
				
				// Get maximum volume
				_mindControlOpt2_maxVol = _mindControlOpt2_am.getStreamMaxVolume(
						AudioManager.STREAM_MUSIC);
				
				// Reset current volume
				_mindControlOpt2_am.setStreamVolume(AudioManager.STREAM_MUSIC,
						_mindControlOpt2_maxVol/2, 0);
				
				// Get current volume
				_mindControlOpt2_currVol = _mindControlOpt2_am.getStreamVolume(
						AudioManager.STREAM_MUSIC);
				
				_tv_mindControlOpt2_volume.setText("Current Volume: " +
						_mindControlOpt2_currVol + " (Max: " + 
						_mindControlOpt2_maxVol + ")");
				break;
				
			case R.id.rb_mindControl_opt3:
				_activeMindControlActivity = 3;
				//
				break;
			}
			_bMindControl = true;	
			
			break;
			
		case R.id.btn_mindControl_end:
			// Hide mind control menu and show main menu
			toggleMindControlMenu(false);
			toggleMainMenu(true);
			
			_bMindControl = false;
			_activeMindControlActivity = 0;
			break;
			
		case R.id.btn_mindControlOpt1_end:
			
			_bMindControl = false;
			_activeMindControlActivity = 0;
			
			// Hide mind control option 1, show mind control menu
			toggleOption_1_Views(false);
			toggleMindControlMenu(true);
			break;
			
		case R.id.btn_mindControlOpt2_end:
			
			_bMindControl = false;
			_activeMindControlActivity = 0;
			
			if(_mindControlOpt2_mp != null) {
				_mindControlOpt2_mp.stop();
				_mindControlOpt2_mp.reset();
				_mindControlOpt2_mp.release();
				_mindControlOpt2_mp = null;
			}
			
			// Hide mind control option 2, show mind control menu
			toggleOption_2_Views(false);
			toggleMindControlMenu(true);
			break;

		default:
			// Do nothing
			break;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		_kValue = Integer.parseInt(
				parent.getItemAtPosition(position).toString());
		Log.d("OnItemSelected", "New k value: " + _kValue);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	private void clearWindow() {
		
		_eeg_Window.get(0).clear();
		_eeg_Window.get(1).clear();
		_eeg_Window.get(2).clear();
		_eeg_Window.get(3).clear();
	}
	
	private void clearBuffer(int bufferIndex) {
		
		_arr_TP9.get(bufferIndex).clear();
		_arr_FP1.get(bufferIndex).clear();
		_arr_FP2.get(bufferIndex).clear();
		_arr_TP10.get(bufferIndex).clear();
	}
	
	private void clearAllBuffers() {
		for(int buffer = 0; buffer < BUFFERS; buffer++) {
			clearBuffer(buffer);
		}
	}
	
	private void processEegWindow() {
		
		Double[] arr_TP9 = _eeg_Window.get(0).toArray(
				new Double[_eeg_Window.get(0).size()]);
		Double[] arr_FP1 = _eeg_Window.get(1).toArray(
				new Double[_eeg_Window.get(1).size()]);
		Double[] arr_FP2 = _eeg_Window.get(2).toArray(
				new Double[_eeg_Window.get(2).size()]);
		Double[] arr_TP10 = _eeg_Window.get(3).toArray(
				new Double[_eeg_Window.get(3).size()]);

		new FFT_Task().execute(arr_TP9, arr_FP1, arr_FP2, arr_TP10);
	}
	
	private void processBuffer(int bufferIndex) {
		
		Double [] arr_TP9 = _eeg_Window.get(0).toArray(
				new Double[_eeg_Window.get(0).size()]);
		Double [] arr_FP1 = _eeg_Window.get(1).toArray(
				new Double[_eeg_Window.get(1).size()]);
		Double [] arr_FP2 = _eeg_Window.get(2).toArray(
				new Double[_eeg_Window.get(2).size()]);
		Double [] arr_TP10 = _eeg_Window.get(3).toArray(
				new Double[_eeg_Window.get(3).size()]);
			
		// Shift the older samples out of the array
		int reusedSamples = WINDOW_SIZE - SHIFT_AMT;
		int srcPos = WINDOW_SIZE - reusedSamples - 1;		
		
		System.arraycopy(arr_TP9, srcPos, arr_TP9, 0, reusedSamples);
		System.arraycopy(arr_FP1, srcPos, arr_FP1, 0, reusedSamples);
		System.arraycopy(arr_FP2, srcPos, arr_FP2, 0, reusedSamples);
		System.arraycopy(arr_TP10, srcPos, arr_TP10, 0, reusedSamples);
		
		Double [] buffer_TP9 = _arr_TP9.get(bufferIndex).toArray(
				new Double[_arr_TP9.get(bufferIndex).size()]);
		Double [] buffer_FP1 = _arr_FP1.get(bufferIndex).toArray(
				new Double[_arr_FP1.get(bufferIndex).size()]);
		Double [] buffer_FP2 = _arr_FP2.get(bufferIndex).toArray(
				new Double[_arr_FP2.get(bufferIndex).size()]);
		Double [] buffer_TP10 = _arr_TP10.get(bufferIndex).toArray(
				new Double[_arr_TP10.get(bufferIndex).size()]);
		
		// Copy the newer samples to the end of the array
		System.arraycopy(buffer_TP9, 0, arr_TP9, reusedSamples, SHIFT_AMT);
		System.arraycopy(buffer_FP1, 0, arr_FP1, reusedSamples, SHIFT_AMT);
		System.arraycopy(buffer_FP2, 0, arr_FP2, reusedSamples, SHIFT_AMT);
		System.arraycopy(buffer_TP10, 0, arr_TP10, reusedSamples, SHIFT_AMT);	

		new FFT_Task().execute(arr_TP9, arr_FP1, arr_FP2, arr_TP10);

		clearBuffer(bufferIndex);
	}
	
	// new FFT_Task().execute(arr_TP9, arr_FP1, arr_FP2, arr_TP10);
	private class FFT_Task extends AsyncTask<Double [], Void, Double [][]> {
		
		// Write things to do for processing raw EEG data here
		// rawEEGData[0]: TP9, rawEEGData[1]: FP1,
		// rawEEGData[2]: FP2, rawEEGData[3]: TP10
		protected Double [][] doInBackground(Double []... rawEEGData) {
			
			int numSamples = rawEEGData[0].length;
			
			Double [] features = new Double[16];
			int featureIndex = 0;
			
			// Ensure that there are WINDOW_SIZE samples
			if(numSamples == WINDOW_SIZE) {
				for(int pos = 0; pos < 4; pos++) {
					
					// Cast from Double to double
					double [] x = new double[numSamples];
					for (int currX = 0; currX < numSamples; currX++) {
						x[currX] = (double) rawEEGData[pos][currX];
					}
					
					// 'y' array for storing the imaginary part
					double [] y = new double[numSamples];
					
					// Perform fft
					FFT fft = new FFT(WINDOW_SIZE);
					fft.fft(x, y);
					
					// Find the log of absolute power, log[sqrt(x*x + y*x)]
					double [] logAbsPower = new double[numSamples];
					for (int curr = 0; curr < numSamples; curr++) {
						logAbsPower[curr] = Math.log10(Math.sqrt(
								(x[curr] * x[curr] + y[curr] * y[curr]) ));
					}
					
					double thetaPower = 0.0;
					double alphaPower = 0.0;
					double betaPower = 0.0;
					double gammaPower = 0;
					
					double thetaSamples = 0.0;
					double alphaSamples = 0.0;
					double betaSamples = 0.0;
					double gammaSamples = 0.0;		
					
					// Average sampling frequency
					double samplingFreq = 220;
					
					// logAbsPower[1] to logAbsPower[numSamples/2]
					for (double freqBand = 1; freqBand <= (numSamples / 2); freqBand++) {
						
						double lowerBandLimit = samplingFreq / (numSamples / 2.0)
								* (freqBand - 1);
						double upperBandLimit = samplingFreq / (numSamples / 2.0)
								* freqBand;
						
						// Theta (4 - 8 Hz)
						if(lowerBandLimit >= 4 && upperBandLimit <= 8) {
							thetaPower += logAbsPower[(int) freqBand];
							thetaSamples++;
						}
						// Alpha (8 - 14 Hz)
						else if(lowerBandLimit >= 8 && upperBandLimit <= 14) {
							alphaPower += logAbsPower[(int) freqBand];
							alphaSamples++;
						}
						// Beta (14 - 30 Hz)
						else if(lowerBandLimit >= 14 && upperBandLimit <= 30) {
							betaPower += logAbsPower[(int) freqBand];
							betaSamples++;
						}
						// Gamma (30 - 40 Hz)
						else if(lowerBandLimit >= 30 && upperBandLimit <= 40) {
							gammaPower += logAbsPower[(int) freqBand];
							gammaSamples++;
						}
					}
					
					thetaPower /= thetaSamples;
					alphaPower /= alphaSamples;
					betaPower /= betaSamples;
					gammaPower /= gammaSamples;
					
					// Store values inside the features array
					features[featureIndex + 0] = thetaPower;
					features[featureIndex + 1] = alphaPower;
					features[featureIndex + 2] = betaPower;
					features[featureIndex + 3] = gammaPower;
					featureIndex += 4;
					
				} // End for loop
				
			} // End if
			
			Double [][] result = new Double[5][];
			result[0] = features;
			result[1] = rawEEGData[0];
			result[2] = rawEEGData[1];
			result[3] = rawEEGData[2];
			result[4] = rawEEGData[3];
			
			return result;
		}
		
		protected void onPostExecute(Double [][] features) {
			
			// Not in mind control mode, save to temp features for saving later
			if(!_bMindControl)
				_tempFeatures.add(features);
			else
			{
				// In Mind Control Mode, convert features into a feature vector
				// Find k nearest neighbours and determine its associated class			
				double [] testFeatures = unboxDouble(features[0]);
				FeatureVector fvTest = new FeatureVector(null, testFeatures, 0);
				fvTest.setFeatureClass(findFeatureClass(fvTest, _combinedFvArr));
				
				/*Log.d("fvDist", "Testing fv feature class: " +
						fvTest.getFeatureClass());*/
				
				boolean bIsFocus = fvTest.getFeatureClass() == FOCUS_CLASS_LABEL;
				performMindControl(bIsFocus);
			}
		}
	}
	
	/**
	 * Perform some 'mind control' activities here,<br>
	 * using the status (Focus/Relax) of the current feature vector 
	 * 
	 * @param bIsFocus The status (Focus/Relax) of the current feature vector
	 */
	private void performMindControl(boolean bIsFocus) {
		
		switch (_activeMindControlActivity)
		{
		case 1:
			if (bIsFocus)
				_tv_mindControlOpt1_status.setText("IN A FOCUSED STATE");
			else
				_tv_mindControlOpt1_status.setText("IN A RELAXED STATE");
			break;

		case 2:		
			System.out.println("Curr Vol: " + _mindControlOpt2_currVol +
					" Max Vol: " + _mindControlOpt2_maxVol);
			_tv_mindControlOpt2_volume.setText("Current Volume: " +
					_mindControlOpt2_currVol + " (Max: " + 
					_mindControlOpt2_maxVol + ")");
			
			if (bIsFocus) {
				if(_mindControlOpt2_currVol < _mindControlOpt2_maxVol) {
					_mindControlOpt2_currVol += 1;
				}
				if(_mindControlOpt2_currVol > _mindControlOpt2_maxVol) {
					_mindControlOpt2_currVol = _mindControlOpt2_maxVol;
				}
			}
			else {
				if(_mindControlOpt2_currVol > 0) {
					_mindControlOpt2_currVol -= 1;
				}
				if(_mindControlOpt2_currVol < 0) {
					_mindControlOpt2_currVol = 0;
				}
			}
			
			_mindControlOpt2_am.setStreamVolume(AudioManager.STREAM_MUSIC,
					_mindControlOpt2_currVol, 0);
			break;

		case 3:
			break;

		default:
			// Do nothing
			break;
		}
	}
	
	private void playAlarm() {
		MediaPlayer mp;
        mp = MediaPlayer.create(MindControlActivity.this, R.raw.nuclear_alarm);
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp = null;
            }

        });
        mp.start();
	}
	
	private void playMusic() {
		
		_mindControlOpt2_mp = MediaPlayer.create(MindControlActivity.this,
				R.raw.legends_of_fall_short);
		_mindControlOpt2_mp.setLooping(true);
		_mindControlOpt2_mp.start();
	}
	
	private void saveRelaxData() {
		
		// Create feature vectors from the collected features
		for(Double [][] features : _tempFeatures) {
			
			double [] fvFeatures = unboxDouble(features[0]);
			double [] rawEEGValues_TP9 = unboxDouble(features[1]);
			double [] rawEEGValues_FP1 = unboxDouble(features[2]);
			double [] rawEEGValues_FP2 = unboxDouble(features[3]);
			double [] rawEEGValues_TP10 = unboxDouble(features[4]);
			
			double [][] rawEEGValues = new double [4][];
			for(int i = 0; i < rawEEGValues_TP9.length; i++) {
				rawEEGValues[0][i] = rawEEGValues_TP9[i];
				rawEEGValues[1][i] = rawEEGValues_FP1[i];
				rawEEGValues[2][i] = rawEEGValues_FP2[i];
				rawEEGValues[3][i] = rawEEGValues_TP10[i];
			}
			
			FeatureVector fv = new FeatureVector(rawEEGValues,
					fvFeatures, RELAX_CLASS_LABEL);
			
			if(_relaxFv.size() >= MAX_QUEUE_SIZE) {
				_relaxFv.removeFirst();
			}
			_relaxFv.addLast(fv);
		}
		
		saveToFile(DataType.RelaxData);
		_tempFeatures.clear();
	}
	
	private void saveFocusData() {
		
		// Create feature vectors from the collected features
		for(Double [][] features : _tempFeatures) {
			
			double [] fvFeatures = unboxDouble(features[0]);
			double [] rawEEGValues_TP9 = unboxDouble(features[1]);
			double [] rawEEGValues_FP1 = unboxDouble(features[2]);
			double [] rawEEGValues_FP2 = unboxDouble(features[3]);
			double [] rawEEGValues_TP10 = unboxDouble(features[4]);
			
			double [][] rawEEGValues = new double [4][];
			for(int i = 0; i < rawEEGValues_TP9.length; i++) {
				rawEEGValues[0][i] = rawEEGValues_TP9[i];
				rawEEGValues[1][i] = rawEEGValues_FP1[i];
				rawEEGValues[2][i] = rawEEGValues_FP2[i];
				rawEEGValues[3][i] = rawEEGValues_TP10[i];
			}
			
			FeatureVector fv = new FeatureVector(rawEEGValues,
					fvFeatures, FOCUS_CLASS_LABEL);
			
			if(_focusFv.size() >= MAX_QUEUE_SIZE) {
				_focusFv.removeFirst();
			}
			_focusFv.addLast(fv);
		}
		
		saveToFile(DataType.FocusData);
		_tempFeatures.clear();
	}
	
	private void saveToFile(DataType dataType) {

		// Create directory in SDCard
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/museIOFiles");
		
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		String fileName = dataType.toString() + ".dat";
		File file = new File(dir, fileName);
		
		String textFileName = "trainingData.txt";
		File textFile = new File(dir, textFileName);

		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		OutputStreamWriter osw = null;

		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			
			if(dataType == DataType.RelaxData)
				out.writeObject(_relaxFv);
			else if(dataType == DataType.FocusData)
				out.writeObject(_focusFv);
			
			out.close();
			
			Log.d("saveToFile()",
					"Successfully saved '" + dataType.toString() + "' data!");
			
			// Save training feature vectors as text
			fos = new FileOutputStream(textFile);
			osw = new OutputStreamWriter(fos);
			
			osw.write(convertTrainingDataToText());
			osw.flush();
			osw.close();
			
			Log.d("saveToFile()", "Successfully saved training data!");
			
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to save '" + dataType.toString() + "' data!");
			Log.d("saveToFile()",
					"Unable to save '" + dataType.toString() + "' data!");
		} catch (IOException ex) {
			System.out.println("Unable to save '" + dataType.toString() + "' data!");
			Log.d("saveToFile()",
					"Unable to save '" + dataType.toString() + "' data!");
		} catch (Exception ex) {
			System.out.println("Unable to save '" + dataType.toString() + "' data!");
			Log.d("saveToFile()",
					"Unable to save '" + dataType.toString() + "' data!");
		}
	}
	
	private void loadFvData() {
		
		// Create directory in SDCard
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/museIOFiles");

		if (!dir.exists()) {
			Log.d("loadFvData()", "Directory does not exist");
			return;
		}

		String relaxFileName = DataType.RelaxData.toString() + ".dat";
		String focusFileName = DataType.FocusData.toString() + ".dat";
		
		File relaxFile = new File(dir, relaxFileName);
		File focusFile = new File(dir, focusFileName);
				
		FileInputStream fis = null;
		ObjectInputStream in = null;
		Object obj = null;
		
		try
		{
			fis = new FileInputStream(relaxFile);
			in = new ObjectInputStream(fis);
			
			obj = in.readObject();

			if (obj instanceof ArrayDeque<?>)
			{
				ArrayDeque<?> al = (ArrayDeque<?>) obj;

				if (al.size() > 0)
				{
					int size = al.size();
					for (int objIndex = 0; objIndex < size; objIndex++)
					{
						Object childObj = al.getFirst();
						
						if (childObj instanceof FeatureVector)
						{
							_relaxFv.add(((FeatureVector) childObj));
						}
						
						al.removeFirst();
					}
				}
			}

			in.close();
			
			if(!_relaxFv.isEmpty())
			{
				System.out.println("'Relax' data loaded successfully!");
				Log.d("loadFvData()", "'Relax' data loaded successfully!");
			}
			
			fis = new FileInputStream(focusFile);
			in = new ObjectInputStream(fis);
			
			obj = in.readObject();

			if (obj instanceof ArrayDeque<?>)
			{
				ArrayDeque<?> al = (ArrayDeque<?>) obj;

				if (al.size() > 0)
				{
					int size = al.size();
					for (int objIndex = 0; objIndex < size; objIndex++)
					{
						Object childObj = al.getFirst();
						
						if (childObj instanceof FeatureVector)
						{
							_focusFv.add(((FeatureVector) childObj));
						}
						
						al.removeFirst();
					}
				}
			}

			in.close();
			
			if(!_focusFv.isEmpty())
			{
				System.out.println("'Focus' data loaded successfully!");
				Log.d("loadFvData()", "'Focus' data loaded successfully!");
			}
			
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to load 'Relax/Focus' data!");
			Log.d("loadFvData()", "Unable to load 'Relax/Focus' data!");
		} catch (IOException ex) {
			System.out.println("Unable to load 'Relax/Focus' data!");
			Log.d("loadFvData()", "Unable to load 'Relax/Focus' data!");
		} catch (ClassNotFoundException ex) {
			System.out.println("Unable to load 'Relax/Focus' data!");
			Log.d("loadFvData()", "Unable to load 'Relax/Focus' data!");
		} catch (Exception ex) {
			System.out.println("Unable to load 'Relax/Focus' data!");
			Log.d("loadFvData()", "Unable to load 'Relax/Focus' data!");
		}
	}
	
	/**
	 * Converts the feature vectors to their text form
	 * for saving to a text file
	 * 
	 * @return The string containing all training data
	 */
	private String convertTrainingDataToText() {
		
		StringBuilder sb = new StringBuilder();
		
		for(FeatureVector fv : _relaxFv) {
			sb.append(fv.toString() + "\r\n");
		}
		
		for(FeatureVector fv : _focusFv) {
			sb.append(fv.toString() + "\r\n");
		}
		
		return sb.toString();
	}
	
	private void initTrainingData() {
		
		// Do pre-processing for the relax & focus data				
		FeatureVector [] relaxFvArr = Arrays.copyOf(_relaxFv.toArray(),
				_relaxFv.size(), FeatureVector[].class);
		FeatureVector [] focusFvArr = Arrays.copyOf(_focusFv.toArray(),
				_focusFv.size(), FeatureVector[].class);
		
		int combinedLength = relaxFvArr.length + focusFvArr.length;
		_combinedFvArr = new FeatureVector[combinedLength];
		System.arraycopy(relaxFvArr, 0, _combinedFvArr,
				0, relaxFvArr.length);
		System.arraycopy(focusFvArr, 0, _combinedFvArr,
				relaxFvArr.length, focusFvArr.length);
		
		// Calculate standard deviation array for training feature vectors
		calculateStdDev();
	}
	
	private double [] unboxDouble(Double [] doubles) {
		
		double [] unboxedDoubles = new double[doubles.length];
		for (int index = 0; index < doubles.length; index++) {
			unboxedDoubles[index] = (double) doubles[index];
		}
		
		return unboxedDoubles;
	}
	
	private void calculateStdDev() {
		
		double [] meanArr = new double[16];
		double [] varianceArr = new double[16];
		
		for (FeatureVector fv : _combinedFvArr) {
			double [] features = fv.getFeatures();
			
			int index = 0;
			for(double feature : features) {
				meanArr[index] += feature;
				index++;
			}
		}
		
		for(int i = 0; i < 16; i++) {
			meanArr[i] /= _combinedFvArr.length;
		}
		
		for (FeatureVector fv : _combinedFvArr) {
			double [] features = fv.getFeatures();
			
			int index = 0;
			for(double feature : features) {
				varianceArr[index] +=
						(meanArr[index] - feature) * (meanArr[index] - feature);
				index++;
			}
		}
		
		for(int j = 0; j < 16; j++) {
			varianceArr[j] /= _combinedFvArr.length;
		}
		
		_stdDevArr = new double[16];
		for(int k = 0; k < 16; k++) {
			_stdDevArr[k] = Math.sqrt(varianceArr[k]);
		}
	}
	
	/**
	 * Finds the class of the feature vector
	 * 
	 * @param fvTest The testing feature vector
	 * @param trainingFv The set of training feature vectors
	 * 
	 * @return The class of the feature vector (1: Focus, 2: Relax)
	 */
	private int findFeatureClass(FeatureVector fvTest, FeatureVector[] trainingFv) {
	
		int numTrainingFv = trainingFv.length;
		List<FVDist> fvDistList = new ArrayList<FVDist>(numTrainingFv);
		for(int i = 0; i < numTrainingFv; i++) {
			
			double distance = fvTest.calcStdEuclideanDist(
					trainingFv[i], _stdDevArr);
			FVDist newFVDist = new FVDist(
					trainingFv[i].getFeatureClass(), distance);
			fvDistList.add(newFVDist);
		}
		
		Collections.sort(fvDistList);
		//Log.d("fvDist", _kValue + "-nearest neighbours: ");
		
		int numRelaxFeatures = 0;
		int numFocusFeatures = 0;
		for(int i = 0; i < _kValue; i++) {
			
			/*Log.d("fvDist", fvDistList.get(i).getFeatureClass() + ", "
					+ fvDistList.get(i).getDistance());*/
			int featureClass = fvDistList.get(i).getFeatureClass();
			if(featureClass == FOCUS_CLASS_LABEL)
			{
				numFocusFeatures++;
			}
			else if(featureClass == RELAX_CLASS_LABEL)
			{
				numRelaxFeatures++;
			}
		}
		
		if(_bMindControl && (_activeMindControlActivity == 1)) {
			_pb_mindControlOpt1_focusStatus.setProgress(numFocusFeatures);
			_pb_mindControlOpt1_relaxStatus.setProgress(numRelaxFeatures);
		}
		else if(_bMindControl && (_activeMindControlActivity == 2)) {
			_pb_mindControlOpt2_focusStatus.setProgress(numFocusFeatures);
			_pb_mindControlOpt2_relaxStatus.setProgress(numRelaxFeatures);
		}
		
		// Return the feature class for testing feature vector
		return ((numFocusFeatures > numRelaxFeatures) ? FOCUS_CLASS_LABEL
				: RELAX_CLASS_LABEL);
	}
	
	private double [] performCrossValidation() {
		
		int numTrainingFv = _combinedFvArr.length;
		Log.d("performCrossValidation", "# of training fv: " + numTrainingFv);
		
		int testingSetStart = 0;
		int testingSetSize = numTrainingFv/10;
		int testingSetEnd = testingSetSize;
		
		int accIndex = 0;
		double [] accuracyArr = new double[10];
		
		// First 10% with no class, First 10% with the actual class
		// Remaining 90% with the actual class
		// Test the first 10% (No class) with the remaining 90%
		// Find the 'predicted class' for the first 10%
		// Compare the 'predicted class' of first 10% with the actual class
		// Calculate accuracy, Move to the next 10%
		// Sum up all accuracy, divide by 10
		while(testingSetEnd <= numTrainingFv) {
			
			FeatureVector [] testingSet = new FeatureVector[testingSetSize];
			FeatureVector [] validationSet = new FeatureVector[testingSetSize];
			
			System.arraycopy(_combinedFvArr, testingSetStart,
					validationSet, 0, testingSetSize);
			
			for(int i = 0; i < testingSetSize; i++) {
				testingSet[i] = new FeatureVector(null,
						_combinedFvArr[testingSetStart+i].getFeatures(), 0);
			}
			
			int trainingSetSize = numTrainingFv - testingSetSize;
			FeatureVector [] trainingSet = new FeatureVector[trainingSetSize];
			System.arraycopy(_combinedFvArr, 0,
					trainingSet, 0, testingSetStart);
			System.arraycopy(_combinedFvArr, testingSetEnd,
					trainingSet, testingSetStart, numTrainingFv - testingSetEnd);
			
			int numCorrectFv = 0;
			for(int j = 0; j < testingSetSize; j++) {
				testingSet[j].setFeatureClass(findFeatureClass(
						testingSet[j], trainingSet));
				
				/*System.out.println("Predicted: " + testingSet[j].getFeatureClass());
				System.out.println("Actual: " + validationSet[j].getFeatureClass());*/
				
				if(testingSet[j].getFeatureClass() ==
						validationSet[j].getFeatureClass())
					numCorrectFv++;
			}
			
			accuracyArr[accIndex++] = numCorrectFv;
			/*System.out.println("# of correctly predicted fv: " + numCorrectFv);*/
			
			testingSetStart = testingSetEnd;
			testingSetEnd += testingSetSize;
		}
		
		return accuracyArr;
	}
	
	private class CrossValidationTask extends AsyncTask<Void, Void, Double []> {
		
		protected void onPreExecute() {
			
			// Update with 'k' value information and reset the results
			_tv_cv_k_value.setText("Current 'k' value: " + _kValue);
			_tv_cv_results.setText("");
			
			_progDialog_crossValidation =
					new ProgressDialog(MindControlActivity.this);
			_progDialog_crossValidation.setTitle("10-fold Cross Validation");
			_progDialog_crossValidation.setMessage(
					"Performing 10-fold Cross Validation..");
			_progDialog_crossValidation.setCancelable(false);
			_progDialog_crossValidation.setCanceledOnTouchOutside(false);
			_progDialog_crossValidation.show();
		}
		
		protected Double [] doInBackground(Void... params) {
						
			// Initialize training vectors
			initTrainingData();

			// Start cross-validation
			double[] result = performCrossValidation();
			Double[] results = new Double[result.length];
			
			for(int i = 0; i < result.length; i++) {
				results[i] = Double.valueOf(result[i]);
			}
			
			// Just to allow the progress dialog to be shown
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return results;
		}

		protected void onPostExecute(Double [] result) {
			
			_progDialog_crossValidation.dismiss();
			
			double [] results = unboxDouble(result);
			
			int sumAcc = 0;
			String displayedResult =
					"# of correctly identified feature vectors:\n";
			for(double accResult : results) {
				sumAcc += accResult;
				Log.d("CrossValidationTask", "Accuracy: " + accResult);
				displayedResult += ((int) accResult) + "/10\n";
			}
			
			Log.d("CrossValidationTask", "Accuracy: " + sumAcc + "%");
			displayedResult += "\n\nAverage Accuracy: " + sumAcc + "%";
			_tv_cv_results.setText(displayedResult);
		}
	}
	
	private class FVDist implements Comparable<FVDist> {
		private int _featureClass;
		private double _distance;
		
		FVDist(int featureClass, double distance) {
			_featureClass = featureClass;
			_distance = distance;
		}
		
		public int getFeatureClass() {
			return _featureClass;
		}
		
		public double getDistance() {
			return _distance;
		}

		@Override
		public int compareTo(FVDist otherFVDist) {
			if(_distance < otherFVDist.getDistance())
				return -1;
			else if(_distance > otherFVDist.getDistance())
				return 1;
			else
				return 0;
		}
	}
	
	/**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }
        
        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            _museConnectionState = current;
			final String status = "Connection Status:\n"
					+ p.getPreviousConnectionState().toString() + " -> "
					+ current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                                " " + status;
            Log.i("Muse connection", full);
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _tv_main_muse_selector.setText(status);
                        
                        if(current != ConnectionState.CONNECTED) {
                        	_tv_main_horseshoe_tp9.setText(R.string.x);
    						_tv_main_horseshoe_fp1.setText(R.string.x);
    						_tv_main_horseshoe_fp2.setText(R.string.x);
    						_tv_main_horseshoe_tp10.setText(R.string.x);
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case EEG:
                    updateEEG(p.getValues());
                    break;
                    
                case HORSESHOE:
                	updateHorseshoe(p.getValues());
                	break;
                    
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

		private void updateEEG(final ArrayList<Double> data) {
			Activity activity = activityRef.get();
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Append incoming data to arraylists
						// if in recording mode
						if (_bRecordingRelax || _bRecordingFocus || _bMindControl)
						{
							// Fill the window if not yet filled
							if (_eeg_Window.get(0).size() < WINDOW_SIZE) {

								_eeg_Window.get(0).add(
										data.get(Eeg.TP9.ordinal()));
								_eeg_Window.get(1).add(
										data.get(Eeg.FP1.ordinal()));
								_eeg_Window.get(2).add(
										data.get(Eeg.FP2.ordinal()));
								_eeg_Window.get(3).add(
										data.get(Eeg.TP10.ordinal()));
								
								// Process the first filled window
								if(_eeg_Window.get(0).size() ==
										WINDOW_SIZE) {
									
									processEegWindow();
								}
								
							} else {

								// Window filled, start filling buffers
								_arr_TP9.get(_currBuffer).add(
										data.get(Eeg.TP9.ordinal()));
								_arr_FP1.get(_currBuffer).add(
										data.get(Eeg.FP1.ordinal()));
								_arr_FP2.get(_currBuffer).add(
										data.get(Eeg.FP2.ordinal()));
								_arr_TP10.get(_currBuffer).add(
										data.get(Eeg.TP10.ordinal()));

								// Check for filled buffer
								if (_arr_TP9.get(_currBuffer).size() == SHIFT_AMT) {

									// Process filled buffer
									processBuffer(_currBuffer);
									
									// Switch to next buffer
									_currBuffer = (_currBuffer + 1) % BUFFERS;
								}

							}

						} // End if
					} // End run
				});
			}
		} // End updateEEG
		
		private void updateHorseshoe(final ArrayList<Double> data) {
			Activity activity = activityRef.get();
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						
						int tp9 = data.get(0).intValue();
						int fp1 = data.get(1).intValue();
						int fp2 = data.get(2).intValue();
						int tp10 = data.get(3).intValue();

						/*Log.i("Horseshoe values: ", tp9 + ", " + fp1 + ", "
								+ fp2 + ", " + tp10);*/

						_tv_main_horseshoe_tp9.setText(tp9 == 1 ? "Good"
								: (tp9 == 2 ? "Okay" : "Bad!"));
						_tv_main_horseshoe_fp1.setText(fp1 == 1 ? "Good"
								: (fp1 == 2 ? "Okay" : "Bad!"));
						_tv_main_horseshoe_fp2.setText(fp2 == 1 ? "Good"
								: (fp2 == 2 ? "Okay" : "Bad!"));
						_tv_main_horseshoe_tp10.setText(tp10 == 1 ? "Good"
								: (tp10 == 2 ? "Okay" : "Bad!"));
						
					} // End run
				});
			}
		} // End updateHorseshoe
		
    }

}
