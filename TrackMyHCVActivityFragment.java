package com.purohit.rethink_hcv.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.purohit.rethink_hcv.R;
import com.purohit.rethink_hcv.activity.MainActivity;
import com.purohit.rethink_hcv.adapters.TrackHCVAdapter;
import com.purohit.rethink_hcv.entity.TrackInput;
import com.purohit.rethink_hcv.mainBase.AppController;
import com.purohit.rethink_hcv.mainBase.BaseFragment;
import com.purohit.rethink_hcv.model.GetTrackHCVEntity;
import com.purohit.rethink_hcv.model.SuccessResponse;
import com.purohit.rethink_hcv.services.APIRequestHandler;
import com.purohit.rethink_hcv.utils.AppConstants;
import com.purohit.rethink_hcv.utils.DialogManager;
import com.purohit.rethink_hcv.utils.GlobalMethods;
import com.purohit.rethink_hcv.utils.MyAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This class implements UI and functions for Instructions
 */


public class TrackMyHCVActivityFragment extends BaseFragment {

    @BindView(R.id.activity_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.chart_lay)
    LinearLayout mChartLay;

    @BindView(R.id.table_lay)
    LinearLayout mTableLay;

    /*total values*/
    @BindView(R.id.txt_screenings)
    TextView mScreenTotalTxt;

    @BindView(R.id.txt_diagnoses)
    TextView mDiagnoseTotalTxt;

    @BindView(R.id.txt_referrals)
    TextView mReferTotalTxt;


    private int mDate, mMonth, mYear;

    private SimpleDateFormat mLocalDateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
    private SimpleDateFormat mServerFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    private String mCurrentDateStr = "";
    private TrackHCVAdapter mTrackHCVAdapter;
    /*Date Text view and values text*/
    private TextView selectedDateTxt, screenvalueTxt, diagnosevalueTxt, referrealvalueTxt;
    private ArrayList<GetTrackHCVEntity> mTrackHcvList = new ArrayList<GetTrackHCVEntity>();

    @BindView(R.id.line_chart)
    LineChart mLineChart;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.frag_track_my_hcv, container, false);

        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, mRootView);
        initview();


        return mRootView;
    }

    private void initview() {
        /*initalize firebase analytics*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        /* Set header title */
        ((MainActivity) getActivity()).setTitleTxt(getString(R.string.track_my_hcv_activity));

        ((MainActivity) getActivity()).mChartImg.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).mChartImg.setImageResource(R.drawable.table_icon);

        getCurrentDate();

        getActivityListApi();


        ((MainActivity) getActivity()).mChartImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*display list of activities*/
                if (((MainActivity) getActivity()).mChartImg.getTag().equals(getActivity().getString(R.string.get_table))) {
                    mTableLay.setVisibility(View.VISIBLE);
                    mChartLay.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).mChartImg.setTag(getActivity().getString(R.string.get_chart));
                    ((MainActivity) getActivity()).mChartImg.setImageResource(R.drawable.graph_icon);
                    trackScreenName("Track My Activity Table");


                } else {
                    /*display the line chart*/
                    mTableLay.setVisibility(View.GONE);
                    mChartLay.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).mChartImg.setTag(getActivity().getString(R.string.get_table));
                    ((MainActivity) getActivity()).mChartImg.setImageResource(R.drawable.table_icon);
                    trackScreenName("Track My Activity Chart");


                }
            }
        });

    }

    /*getting current date*/
    private void getCurrentDate() {
        mCurrentDateStr = mLocalDateFormat.format(new Date());
    }


    /*View OnClick*/
    @OnClick({R.id.add_recent_activity_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_recent_activity_btn:
                trackScreenName(getString(R.string.add_activity_screen));
                setScreenDiagRefDialog();
                break;
        }

    }

    /*popup show for create hcv activity*/
    private void setScreenDiagRefDialog() {
        final Dialog mDialog = DialogManager.getDialog(getActivity(), R.layout.my_hcv_activity_popup);

        ImageView mScreenIncreaimg, mDiagIncreaimg, mReferalIncreaimg, mScreenDecreimg, mDiagDecreaimg,
                mReferaDecreimg, closeImg;
        Button submitbtn;
        LinearLayout mDateSelectLay;

        mScreenIncreaimg = (ImageView) mDialog.findViewById(R.id.img_screening_up);
        mDiagIncreaimg = (ImageView) mDialog.findViewById(R.id.img_diagnose_up);
        mReferalIncreaimg = (ImageView) mDialog.findViewById(R.id.img_referral_up);


        mScreenDecreimg = (ImageView) mDialog.findViewById(R.id.img_screening_down);
        mDiagDecreaimg = (ImageView) mDialog.findViewById(R.id.img_diagnose_down);
        mReferaDecreimg = (ImageView) mDialog.findViewById(R.id.img_referral_down);

        screenvalueTxt = (TextView) mDialog.findViewById(R.id.screening_value_txt);
        diagnosevalueTxt = (TextView) mDialog.findViewById(R.id.diagnose_value_txt);
        referrealvalueTxt = (TextView) mDialog.findViewById(R.id.referral_value_txt);
        selectedDateTxt = (TextView) mDialog.findViewById(R.id.selected_date);
        mDateSelectLay = (LinearLayout) mDialog.findViewById(R.id.date_selected_lay);
        submitbtn = (Button) mDialog.findViewById(R.id.submit_btn);
        closeImg = (ImageView) mDialog.findViewById(R.id.close_img);
        /*set current date value*/
        selectedDateTxt.setText(mCurrentDateStr);

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        /*Increase count onclick*/
        mScreenIncreaimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(true, screenvalueTxt);
            }
        });
        mDiagIncreaimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(true, diagnosevalueTxt);


            }
        });
        mReferalIncreaimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(true, referrealvalueTxt);
            }
        });

        /*Decrease count onclick*/
        mScreenDecreimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(false, screenvalueTxt);
            }
        });

        mDiagDecreaimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(false, diagnosevalueTxt);

            }
        });
        mReferaDecreimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityData(false, referrealvalueTxt);

            }
        });
        mDateSelectLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screenvalueTxt.getText().toString().equalsIgnoreCase(getString(R.string.zero))
                        && diagnosevalueTxt.getText().toString().equalsIgnoreCase(getString(R.string.zero))
                        && referrealvalueTxt.getText().toString().equalsIgnoreCase(getString(R.string.zero))) {
                    DialogManager.showToast(getActivity(), getString(R.string.validation_for_add_activity));
                } else {
                    mDialog.dismiss();

                    Bundle params = new Bundle();
                    params.putString("date", selectedDateTxt.getText().toString());
                    params.putString("referrals", referrealvalueTxt.getText().toString());
                    params.putString("diagnoses", diagnosevalueTxt.getText().toString());
                    params.putString("screenings", screenvalueTxt.getText().toString());
                    mFirebaseAnalytics.logEvent("Activity_Logged", params);

                    addActivityApi(selectedDateTxt.getText().toString(),
                            screenvalueTxt.getText().toString(), diagnosevalueTxt.getText().toString(),
                            referrealvalueTxt.getText().toString());


                }

            }
        });

        //cancel popup outside click or touch
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);

        mDialog.show();


        /*check date values or exist or not in list*/
        setDateValuesifhave(mCurrentDateStr, true);

    }

    private void addActivityApi(String date, String screening, String diaognos, String referrals) {
        Date selectedDate;
        String mDateStr;
        TrackInput trackInput = new TrackInput();


        SimpleDateFormat localDateFormat = new SimpleDateFormat("MMM dd yy", Locale.ENGLISH);

        try {
            selectedDate = localDateFormat.parse(date);
            mDateStr = localDateFormat.format(selectedDate);
            trackInput.setDate(GlobalMethods.getCustomDateFormat(mDateStr, localDateFormat, mServerFormat));
            trackInput.setScreenings(screening);
            trackInput.setDiagnoses(diaognos);
            trackInput.setReferrals(referrals);
            trackInput.setDrProfileID(GlobalMethods.getStringValue(getActivity(), AppConstants.DOCTOR_ID));
            if (GlobalMethods.isNetworkAvailable(getActivity())) {
                APIRequestHandler.getInstance().addHCVActivity(this, trackInput);
            } else {
                DialogManager.showErrorDiag(getActivity(), getString(R.string.internet_error), false);

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    /*call api for get the hcv track activity*/
    private void getActivityListApi() {
        if (GlobalMethods.isNetworkAvailable(getActivity())) {
            APIRequestHandler.getInstance().getHCVActivity(this);
        } else {
            DialogManager.showErrorDiag(getActivity(), getActivity().getString(R.string.internet_error), false);
        }
    }


    /*Show data picker*/

    private void showDatePickerDialog() {

        final Calendar cal = Calendar.getInstance();
        mDate = cal.get(Calendar.DAY_OF_MONTH);
        mMonth = cal.get(Calendar.MONTH);
        mYear = cal.get(Calendar.YEAR);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), mDateSetListener,
                mYear, mMonth, mDate);


        mDatePicker.getDatePicker().setMaxDate(cal.getTimeInMillis());
        mDatePicker.show();


    }

    /*Date picker listener */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog
            .OnDateSetListener() {

        /* date picker dialog box*/
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            mYear = year;
            mMonth = monthOfYear;
            mDate = dayOfMonth;


            String dateStr = String.valueOf(mMonth + 1) + " " + mDate + " " + mYear;
            Date selectedDate;
            String mDateStr;
            SimpleDateFormat localDatePickerFormat = new SimpleDateFormat("MM dd yyyy", Locale.ENGLISH);
            try {
                selectedDate = localDatePickerFormat.parse(dateStr);
                mDateStr = localDatePickerFormat.format(selectedDate);
                selectedDateTxt.setText(GlobalMethods.getCustomDateFormat(mDateStr, localDatePickerFormat, mLocalDateFormat));
                setDateValuesifhave(mDateStr, false);
            } catch (Exception e) {
            }
        }


    };

    private int mChartmaxValue = 0;
    private ArrayList<String> xAxisValue = new ArrayList<>();

    @Override
    public void onRequestSuccess(Object mResObj) {
        super.onRequestSuccess(mResObj);
        if (mResObj instanceof SuccessResponse) {
            SuccessResponse response = (SuccessResponse) mResObj;
            if (response.getMessage().equalsIgnoreCase(getActivity().getString(R.string.success))) {
                getActivityListApi();
            }
        } else {
            mTrackHcvList.clear();
            xAxisValue.clear();
            mTrackHcvList = (ArrayList<GetTrackHCVEntity>) mResObj;
            ArrayList<Integer> arrayList = new ArrayList<Integer>();

            /*set total values*/
            if (mTrackHcvList.size() > 0) {
                int screeningtotal = 0, diagnosetotal = 0, referaltotal = 0;
                for (int i = 0; i < mTrackHcvList.size(); i++) {
                    screeningtotal = screeningtotal + Integer.parseInt(mTrackHcvList.get(i).getScreenings());
                    diagnosetotal = diagnosetotal + Integer.parseInt(mTrackHcvList.get(i).getDiagnoses());
                    referaltotal = referaltotal + Integer.parseInt(mTrackHcvList.get(i).getReferrals());

                    xAxisValue.add(mTrackHcvList.get(i).getDate());
                    arrayList.add(Integer.parseInt(mTrackHcvList.get(i).getScreenings()));
                    arrayList.add(Integer.parseInt(mTrackHcvList.get(i).getDiagnoses()));
                    arrayList.add(Integer.parseInt(mTrackHcvList.get(i).getReferrals()));


                }


                mChartmaxValue = Collections.max(arrayList) + 1;
                mScreenTotalTxt.setText(String.valueOf(screeningtotal) + "\n" + getString(R.string.screenings));
                mDiagnoseTotalTxt.setText(String.valueOf(diagnosetotal) + "\n" + getString(R.string.diagnoses));
                mReferTotalTxt.setText(String.valueOf(referaltotal) + "\n" + getString(R.string.referrals));

                storeLocalValues(screeningtotal, diagnosetotal, referaltotal);
                ((MainActivity) getActivity()).setScDiaRefValuse();


                setAdapter(mTrackHcvList);
                if (xAxisValue.size() > 0) {
                    ArrayList<String> xAxisValues = new ArrayList<>();
                    SimpleDateFormat localDatePickerFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    SimpleDateFormat targetformat = new SimpleDateFormat("M/dd/yy", Locale.ENGLISH);
                    for (int i = 0; i < xAxisValue.size(); i++) {
                        try {
                            Date selectedDate = localDatePickerFormat.parse(xAxisValue.get(i));
                            String mDateStr = localDatePickerFormat.format(selectedDate);
                            xAxisValues.add(GlobalMethods.getCustomDateFormat(mDateStr, localDatePickerFormat, targetformat));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    setChartValue(xAxisValues);

                }
            }


        }


    }


    private void setAdapter(ArrayList<GetTrackHCVEntity> mTrackHcvList) {

        mTrackHCVAdapter = new TrackHCVAdapter(mActivity, mTrackHcvList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mTrackHCVAdapter);


    }

    /*Set the hcv activity values if date have already */
    private void setDateValuesifhave(String mCurrentDateStr, boolean isLocalformat) {
        String date = "";
        boolean ishave = false;
        if (isLocalformat) {
            date = GlobalMethods.getCustomDateFormat(mCurrentDateStr, mLocalDateFormat, mServerFormat);
        } else {
            SimpleDateFormat localeformat = new SimpleDateFormat("MM dd yy", Locale.ENGLISH);
            date = GlobalMethods.getCustomDateFormat(mCurrentDateStr, localeformat, mServerFormat);

        }
        if (mTrackHcvList != null && mTrackHcvList.size() > 0) {
            for (int i = 0; i < mTrackHcvList.size(); i++) {
                if (date.equalsIgnoreCase(mTrackHcvList.get(i).getDate())) {
                    ishave = true;
                    screenvalueTxt.setText(mTrackHcvList.get(i).getScreenings());
                    diagnosevalueTxt.setText(mTrackHcvList.get(i).getDiagnoses());
                    referrealvalueTxt.setText(mTrackHcvList.get(i).getReferrals());
                    break;
                }

            }
            if (!ishave) {
                screenvalueTxt.setText(getString(R.string.zero));
                diagnosevalueTxt.setText(getString(R.string.zero));
                referrealvalueTxt.setText(getString(R.string.zero));
            }
        }

    }


    /*store the total values of screening,diagnose and referral in shared preference*/
    private void storeLocalValues(int screeningtotal, int diagnosetotal, int referaltotal) {
        GlobalMethods.storeStringValue(getActivity(), AppConstants.SCREENINGS_TOTAL, String.valueOf(screeningtotal));
        GlobalMethods.storeStringValue(getActivity(), AppConstants.DIAGNOSE_TOTAL, String.valueOf(diagnosetotal));
        GlobalMethods.storeStringValue(getActivity(), AppConstants.REFERRALS_TOTAL, String.valueOf(referaltotal));

    }

    /*set the chart properties*/
    private void setChartValue(final ArrayList<String> xAxisValues) {


        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mLineChart.getXAxis();
//        xAxis.setLabelCount(5);
//        xAxis.enableGridDashedLine(10f, 10f, 0f);(
        //xAxis.setYOffset(10f);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyAxisValueFormatter());
        //leftAxis.setDrawLabels(false);
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        if (mChartmaxValue >= 7) {
            leftAxis.setLabelCount(7, false);
        } else {
            leftAxis.setLabelCount(mChartmaxValue, false);
        }
        leftAxis.isForceLabelsEnabled();

        // leftAxis.setLabelCount(mChartmaxValue+1, true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMaximum(mChartmaxValue);
        leftAxis.setAxisMinimum(0f);

        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);


        // Format chart
//
        mLineChart.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.gray));
        mLineChart.getXAxis().setDrawLabels(true);
        mLineChart.getXAxis().setAvoidFirstLastClipping(false);

        // Disable zooming
        mLineChart.setPinchZoom(false);
        mLineChart.setScaleXEnabled(true);
        mLineChart.setScaleYEnabled(true);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getXAxis().isForceLabelsEnabled();


        mLineChart.getXAxis().setLabelRotationAngle(-75f);

        mLineChart.getXAxis().setTextSize(7f);
        mLineChart.animateX(2500);
        mLineChart.getDescription().setText("");
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mLineChart.fitScreen();
        mLineChart.setExtraRightOffset(20f);
        mLineChart.setExtraLeftOffset(10f);

        mLineChart.getXAxis().setGranularity(1f);

        if (mTrackHcvList.size() == 1) {
            mLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        } else {
            mLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    return getDate((long) value);

                }
            });
        }
        //

        if (xAxisValues.size() < 6) {
            mLineChart.getXAxis().setLabelCount(xAxisValues.size(), true);
        } else {
            mLineChart.getXAxis().setLabelCount(6, true);
        }
        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        mLineChart.getLegend().setEnabled(false);
        // add data
        mLineChart.invalidate();
        setData();


    }


    /*Draw the line in chart*/
    private void setData() {

        ArrayList<Entry> screeningsDataEntries = new ArrayList<Entry>();
        ArrayList<Entry> diagnosesDataEntries = new ArrayList<Entry>();
        ArrayList<Entry> referralsDataEntries = new ArrayList<Entry>();

        if (mTrackHcvList.size() == 1) {
            //Since we have only one date, WE NEED a Orgin to draw the line in the chart
            screeningsDataEntries.add(new Entry(0, 0));
            diagnosesDataEntries.add(new Entry(0, 0));
            referralsDataEntries.add(new Entry(0, 0));

            screeningsDataEntries.add(new Entry(1, Integer.parseInt(mTrackHcvList.get(0).getScreenings())));
            // screeningsDataEntries.add(new Entry(1, 10f));

            diagnosesDataEntries.add(new Entry(1, Integer.parseInt(mTrackHcvList.get(0).getDiagnoses())));
            // diagnosesDataEntries.add(new Entry(1, 20f));
            referralsDataEntries.add(new Entry(1, Integer.parseInt(mTrackHcvList.get(0).getReferrals())));
            // referralsDataEntries.add(new Entry(1, 30f));

//            screeningsDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(1).getDate()), Integer.parseInt(mTrackHcvList.get(0).getScreenings())));
//            diagnosesDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(1).getDate()), Integer.parseInt(mTrackHcvList.get(0).getDiagnoses())));
//            referralsDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(1).getDate()), Integer.parseInt(mTrackHcvList.get(0).getReferrals())));


            LineDataSet screeningsDataSet, diagnosesDataSet, referralsDataSet;

            // create a dataset and give it a type
            screeningsDataSet = new LineDataSet(screeningsDataEntries, getString(R.string.screenings));
            Entry screenings_Entry = screeningsDataSet.getEntryForIndex(screeningsDataSet.getEntryCount() - 1);

            screeningsDataSet.setDrawCircles(true);
            screeningsDataSet.setDrawCircleHole(true);
            screeningsDataSet.setCircleHoleRadius(2.5f);
            screeningsDataSet.setCircleRadius(7f);
            screeningsDataSet.setLineWidth(3f);
            screeningsDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.yellow));

            int size = screeningsDataSet.getEntryCount() - 1;
            for (int i = 0; i < size; i++) {
                if (i != screenings_Entry.getX()) {
                    screeningsDataSet.removeEntry(screeningsDataSet.getEntryForIndex(i));
                }
            }

            diagnosesDataSet = new LineDataSet(diagnosesDataEntries, getString(R.string.diagnoses));
            Entry diagnoses_Entry = diagnosesDataSet.getEntryForIndex(diagnosesDataSet.getEntryCount() - 1);

            diagnosesDataSet.setDrawCircles(true);
            diagnosesDataSet.setDrawCircleHole(true);
            diagnosesDataSet.setCircleHoleRadius(2.5f);
            diagnosesDataSet.setCircleRadius(7f);

            diagnosesDataSet.setLineWidth(3f);
            diagnosesDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.lite_brown));
            int size1 = diagnosesDataSet.getEntryCount() - 1;
            for (int i = 0; i < size1; i++) {
                if (i != diagnoses_Entry.getX()) {
                    diagnosesDataSet.removeEntry(diagnosesDataSet.getEntryForIndex(i));

                }
            }

            referralsDataSet = new LineDataSet(referralsDataEntries, getString(R.string.referrals));
            Entry referrals_Entry = referralsDataSet.getEntryForIndex(referralsDataSet.getEntryCount() - 1);

            referralsDataSet.setDrawCircles(true);
            referralsDataSet.setDrawCircleHole(true);
            referralsDataSet.setCircleHoleRadius(2.5f);
            referralsDataSet.setCircleRadius(7f);

            referralsDataSet.setLineWidth(3f);
            referralsDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.dark_brown));
            int size2 = referralsDataSet.getEntryCount() - 1;
            for (int i = 0; i < size2; i++) {
                if (i != referrals_Entry.getX()) {
                    referralsDataSet.removeEntry(referralsDataSet.getEntryForIndex(i));

                }
            }


            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(screeningsDataSet);
            dataSets.add(diagnosesDataSet);
            dataSets.add(referralsDataSet);
            LineData data = new LineData(dataSets);
            data.setDrawValues(false);
            data.setHighlightEnabled(false);
            mLineChart.getXAxis().setCenterAxisLabels(true);
            mLineChart.setData(data);


        } else {
            mLineChart.getXAxis().setCenterAxisLabels(false);
            for (int i = 0; i < mTrackHcvList.size(); i++) {


                screeningsDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(i).getDate()), Integer.parseInt(mTrackHcvList.get(i).getScreenings())));
                diagnosesDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(i).getDate()), Integer.parseInt(mTrackHcvList.get(i).getDiagnoses())));
                referralsDataEntries.add(new Entry(conversionmethod(mTrackHcvList.get(i).getDate()), Integer.parseInt(mTrackHcvList.get(i).getReferrals())));

            }


            LineDataSet screeningsDataSet, diagnosesDataSet, referralsDataSet;

//        if (mLineChart.getData() != null &&
//                mLineChart.getData().getDataSetCount() > 0) {
//            screeningsDataSet = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
//            screeningsDataSet.setValues(screeningsDataEntries);
//            mLineChart.getData().notifyDataChanged();
//            mLineChart.notifyDataSetChanged();
//        } else {
            // create a dataset and give it a type
            screeningsDataSet = new LineDataSet(screeningsDataEntries, getString(R.string.screenings));
            screeningsDataSet.setDrawIcons(false);
            screeningsDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.yellow));
            screeningsDataSet.setDrawCircles(false);
            screeningsDataSet.setLineWidth(3f);
            screeningsDataSet.setDrawCircleHole(false);
            screeningsDataSet.setHighlightEnabled(false);
            screeningsDataSet.setDrawValues(false);
            screeningsDataSet.setDrawFilled(false);


            diagnosesDataSet = new LineDataSet(diagnosesDataEntries, getString(R.string.diagnoses));
            diagnosesDataSet.setDrawIcons(false);
            diagnosesDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.lite_brown));
            diagnosesDataSet.setDrawCircles(false);
            diagnosesDataSet.setLineWidth(3f);
            diagnosesDataSet.setDrawCircleHole(false);
            diagnosesDataSet.setHighlightEnabled(false);
            diagnosesDataSet.setDrawValues(false);
            diagnosesDataSet.setDrawFilled(false);

            referralsDataSet = new LineDataSet(referralsDataEntries, getString(R.string.referrals));
            referralsDataSet.setDrawIcons(false);
            referralsDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.dark_brown));
            referralsDataSet.setDrawCircles(false);
            referralsDataSet.setLineWidth(3f);
            referralsDataSet.setHighlightEnabled(false);
            referralsDataSet.setDrawCircleHole(false);
            referralsDataSet.setDrawValues(false);
            referralsDataSet.setDrawFilled(false);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(screeningsDataSet); // add the datasets
            dataSets.add(diagnosesDataSet);
            dataSets.add(referralsDataSet);

            // create a data object with the datasets
            LineData data = new LineData(dataSets);
            //data.setValueFormatter(new ());

            // set data
            mLineChart.setData(data);
        }


    }


    /*add values for activity date*/
    private void setActivityData(boolean isIncrement, TextView textView) {
        int txtInt = Integer.valueOf(textView.getText().toString().trim());
        if ((isIncrement && txtInt >= 0) || (!isIncrement && txtInt > 0)) {
            if (isIncrement) {
                txtInt += 1;
            } else {
                txtInt -= 1;
            }
            textView.setText(String.valueOf(txtInt));
        }

    }

    private long conversionmethod(String date1) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateInString = date1;
        Date date = null;
        try {
            date = sdf.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return sdf.format(calendar.getTime());
    }

}
