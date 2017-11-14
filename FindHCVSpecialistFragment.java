package com.purohit.rethink_hcv.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.purohit.rethink_hcv.R;
import com.purohit.rethink_hcv.activity.MainActivity;
import com.purohit.rethink_hcv.adapters.SpeciaListAdapter;
import com.purohit.rethink_hcv.database.DatabaseUtil;
import com.purohit.rethink_hcv.entity.SearchInput;
import com.purohit.rethink_hcv.mainBase.AppController;
import com.purohit.rethink_hcv.mainBase.BaseFragment;
import com.purohit.rethink_hcv.model.DoctorFavoriteModel;
import com.purohit.rethink_hcv.model.SearchEntity;
import com.purohit.rethink_hcv.services.APIRequestHandler;
import com.purohit.rethink_hcv.utils.AppConstants;
import com.purohit.rethink_hcv.utils.DialogManager;
import com.purohit.rethink_hcv.utils.GPSTracker;
import com.purohit.rethink_hcv.utils.GlobalMethods;
import com.purohit.rethink_hcv.utils.InterfaceBtnCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This class helps in searching list of doctors based on the location entered
 */

public class FindHCVSpecialistFragment extends BaseFragment implements OnMapReadyCallback {


    @BindView(R.id.parent_lay)
    LinearLayout mLinearParentLay;

    @BindView(R.id.search_edt)
    EditText mSearchEdt;

    @BindView(R.id.specialist_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.bottom_lay)
    RelativeLayout mBottomlay;

    @BindView(R.id.no_search_lay)
    RelativeLayout mNosearchlay;

    @BindView(R.id.no_search_txt)
    TextView mNosearcTxt;

    @BindView(R.id.arrow_img)
    ImageView mArrowimg;


    @BindView(R.id.terms_text)
    TextView mTermsCondTxt;
    /*declaration google map variable*/
    private GoogleMap mGoogleMap;
    /*declaration GpsTracker*/
    private GPSTracker gpstrack;

    private ArrayList<SearchEntity> mSearchList = new ArrayList<>();
    private ArrayList<SearchEntity> mLocalSearchFavList = new ArrayList<>();

    private SpeciaListAdapter mSpecialListAdapter = null;
    private String mLatitiude = "", mLongiitude = "";


    private HashMap<Marker, SearchEntity> m_locMarkersHashmap = new HashMap<Marker, SearchEntity>();
    private Marker m_currentMarker;


    private DatabaseUtil db;
    private boolean isMarkerClickForAll = false;
    private LatLngBounds bounds;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private SearchInput searchInput = new SearchInput();
    private String mErrorStr = "";

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.frag_find_hcv_specialist, container, false);
        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(mRootView);
/*ButterKnife for variable initialization*/
        ButterKnife.bind(this, mRootView);
        initializeMap();

        /*intialize database*/
        db = new DatabaseUtil(mActivity);
/* to get the list of doctors added as favorites in Database */
        getFavdoctor();


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        return mRootView;
    }


    private void initializeMap() {
        /* set header title */
        ((MainActivity) getActivity()).setTitleTxt(getString(R.string.find_hcv_specialist));
/* displaying the map */
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
        fragment.getMapAsync(this);

        /*set mylocation button at the bottom of the map*/
        View locationButton = ((View) fragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);

        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                /* to search the values entered in the search box*/

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!mSearchEdt.getText().toString().trim().isEmpty()) {

                        searchInput = splitSearchText(mSearchEdt.getText().toString());
                        DialogManager.showProgress(getActivity());
                        callApi(searchInput);

                        return true;
                    }

                }

                return false;
            }
        });

        /*keypad to be appeared, when a touch made in the search box*/
        mSearchEdt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imms = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imms.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
                return false;
            }
        });

        /*The star symbol designed at the roght top corner has been used for listing the favourite doctors*/
        ((MainActivity) getActivity()).mHeaderStarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((MainActivity) getActivity()).mHeaderStarImg.getTag().equals(getActivity().getString(R.string.get_favorite))
                        ) {
                    getFavdoctor();

                    if (mLocalSearchFavList.size() > 0) {
                        mNosearchlay.setVisibility(View.GONE);
                        mBottomlay.setVisibility(View.VISIBLE);
                        setAdapter(mLocalSearchFavList);
                        isMarkerClickForAll = true;
                        addMarkers(mLocalSearchFavList, mGoogleMap);
                        ((MainActivity) getActivity()).mHeaderStarImg.setTag(getString(R.string.get_all));
                        ((MainActivity) getActivity()).mHeaderStarImg.setImageResource(R.drawable.star_enable_icon);
                    }
                    //else {

//                        if (mGoogleMap != null) {
//                            mGoogleMap.clear();
//                        }
//                        //mNosearchlay.setVisibility(View.VISIBLE);
//                        mNosearcTxt.setText(getString(R.string.no_favorite_doctor));
//                        mBottomlay.setVisibility(View.GONE);
                    // }


                } else {
/* again clicking on the star symbol will make the favorite doctors list invisible */
                    isMarkerClickForAll = false;
                    mNosearchlay.setVisibility(View.GONE);
                    mBottomlay.setVisibility(View.VISIBLE);
                    setAdapter(mSearchList);
                    addMarkers(mSearchList, mGoogleMap);
                    ((MainActivity) getActivity()).mHeaderStarImg.setTag(getString(R.string.get_favorite));
                    ((MainActivity) getActivity()).mHeaderStarImg.setImageResource(R.drawable.star_header_disable);


                }


            }
        });
/* terms and condition screen will be popped up after the installation of application */
        if (!GlobalMethods.isFindHCVSpecialistFirstTime(getActivity())) {
            DialogManager.showTermsConditionDialog(getActivity());
            GlobalMethods.storeValuetoPreference(getActivity(),
                    GlobalMethods.BOOLEAN_PREFERENCE, AppConstants.FIND_HCV_SPECIALIST_FIRST_TIME, true);


        }


    }


    /*
    Method to use the regex to split the user enter text to required data for State,city,zip.
     */
    private SearchInput splitSearchText(String searchText) {

        SearchInput searchInput = new SearchInput();
        String zipPattern = "\\d{5}$";
        String cityPattern = "^([^,]+),";
        String statePattern = "(^|[ ,])[a-zA-Z]{2}($|[ ,])";
        String failsafePattern = "^[a-zA-Z]{2}";

        // Let's check to see if zip code is available
        Matcher matchesResult = tryExpression(zipPattern, searchText);
        if (matchesResult.find()) {
            searchInput.setZip(matchesResult.group());
            return searchInput;
        }

        // If there is no comma in the string, lets add one after the first one

        if (!searchText.contains(",")) {
            matchesResult = tryExpression("\\s(\\w+)$", searchText);
            if (matchesResult.find()) {

                String lastWord = "", firstWord = "";
                lastWord = matchesResult.group().replace(" ", "");
                firstWord = searchText.replaceAll(lastWord, "");

                searchInput.setState(lastWord);
                searchInput.setCity(firstWord);
                return searchInput;
            }

        }

        // Let's check to see if city is available
        matchesResult = tryExpression(cityPattern, searchText);

        if (matchesResult.find()) {

            String city = matchesResult.group().replaceAll(",", "").replaceAll(", ", "");
            searchInput.setCity(city);

            // Let's check to see if state is available
            //matchesResult = tryExpression(statePattern,searchText);
            //if(matchesResult.find()){

            String[] temp = searchText.split(",");
            if (temp.length > 1) {
                String state = temp[temp.length - 1].replaceAll(" ", "");
                searchInput.setState(state);
            }

            // }

            return searchInput;
        }

        // If all else fails, lets just grab the first 2 characters
        matchesResult = tryExpression(failsafePattern, searchText);
        if (matchesResult.find()) {
            String state = matchesResult.group();
            searchInput.setState(state);

            return searchInput;
        }

        return searchInput;

    }

    /* validating the address which is to be searched */
    private Matcher tryExpression(String pattern, String text) {

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        /*if (m.find()) {
            String result = m.group();

            System.out.print("Test:"+ result);
        }*/

        return m;
    }

    @OnClick({R.id.arrow_img, R.id.terms_text})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.arrow_img:
            /*Clicking on arrow icon is used to expand the view */
                if (mArrowimg.getTag() != null && mArrowimg.getTag().toString().equals("1")) {
                    mArrowimg.setImageResource(R.drawable.arrow_line_down);
                    mArrowimg.setTag("2");
                    // mTermsCondTxt.setVisibility(View.VISIBLE);
                    // setAdapter(mSearchList);
                    /*set layout weight for run time*/
                    if (mLinearParentLay.getParent() != null) {
                        mLinearParentLay.removeView(mBottomlay);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 40);
                        mLinearParentLay.addView(mBottomlay, params);
                    }

                } else {
                    mArrowimg.setImageResource(R.drawable.arrow_line_up);
                    mArrowimg.setTag("1");
                    // mTermsCondTxt.setVisibility(View.GONE);
                    // setAdapter(mFirsttwoItemsList);

                     /*set layout weight for run time*/
                    if (mLinearParentLay.getParent() != null) {
                        mLinearParentLay.removeView(mBottomlay);
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 20);
                        mLinearParentLay.addView(mBottomlay, params1);
                    }

                }
                break;


            case R.id.terms_text:
            /* terms and conditions to be popped up*/
                trackScreenName(getString(R.string.terms_condition_screen));
                DialogManager.showTermsConditionDialog(getActivity());
                break;

        }
    }

    /* selected doctor's address will be Zoomed in */
    public void setZoom(SearchEntity searchEntity) {
        LatLng moveloc = new LatLng(Double.parseDouble(searchEntity.getLatitude() != "" ? searchEntity.getLatitude() : "0.0"),
                Double.parseDouble(searchEntity.getLongitude() != "" ? searchEntity.getLongitude() : "0.0"));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(moveloc, 16));
    }

    /*Api call back response*/
    @Override
    public void onRequestSuccess(Object mResObj) {
        super.onRequestSuccess(mResObj);
        DialogManager.hideProgress();
        mNosearchlay.setVisibility(View.GONE);
        mBottomlay.setVisibility(View.VISIBLE);
        mSearchList.clear();
        // mFirsttwoItemsList.clear();
        mSearchList = (ArrayList<SearchEntity>) mResObj;

        if (mSearchList.size() < 10) {
            loopingSearch(searchInput.getRadius());
        } else {
            setAdapter(mSearchList);

           /*set layout weight for run time*/
            if (mLinearParentLay.getParent() != null) {
                mLinearParentLay.removeView(mBottomlay);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 20);
                mLinearParentLay.addView(mBottomlay, params1);

            }
            if (mArrowimg.getTag() != null || mArrowimg.getTag().toString().equals("1")) {
                mArrowimg.setImageResource(R.drawable.arrow_line_up);
                // mTermsCondTxt.setVisibility(View.GONE);
            }
            if (mSearchList.size() > 0) {
                isMarkerClickForAll = false;
                addMarkers(mSearchList, mGoogleMap);

            }
        }


    }


    /*Api call back error response*/
    @Override
    public void onResponseError(String str) {
        super.onResponseError(str);
        DialogManager.hideProgress();
        mErrorStr = str;
        loopingSearch(searchInput.getRadius());

    }

    /*Adapting the items in recyclerview*/
    private void setAdapter(ArrayList<SearchEntity> mSearchList) {
        DialogManager.hideProgress();

        mSpecialListAdapter = new SpeciaListAdapter(mActivity, mSearchList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mSpecialListAdapter);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.clear();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            addPermissions();
            return;
        }
        gpstrack = new GPSTracker(getActivity());
        mGoogleMap.setMyLocationEnabled(true);
/* to check the GPS is on or off */
        if (gpstrack.canGetLocation()) {
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

            getCurrentLatLang();

/* if the GPS is off, an alert message "GPS is not enabled. Do you want to go to settings menu?" will be popped up */
        } else {
            gpstrack.showSettingsAlert();
        }


        searchInput = new SearchInput();
        searchInput.setLatitude(mLatitiude);
        searchInput.setLongitude(mLongiitude);

        DialogManager.showProgress(getActivity());
        callApi(searchInput);


    }

    private void loopingSearch(String radius) {
        int radiusrange = Integer.parseInt(radius);
        switch (radiusrange) {
            case 1:
                searchInput.setRadius("5");
                callApi(searchInput);
                break;
            case 5:
                searchInput.setRadius("10");
                callApi(searchInput);
                break;
            case 10:
                searchInput.setRadius("25");
                callApi(searchInput);
                break;
            case 25:
                searchInput.setRadius("50");
                callApi(searchInput);
                break;
            case 50:
                mNosearchlay.setVisibility(View.VISIBLE);
                mBottomlay.setVisibility(View.GONE);
                mNosearcTxt.setText(mErrorStr);
                if (mGoogleMap != null) {
                    mGoogleMap.clear();
                }
                break;
            default:
                break;
        }

    }

    /* getting the current lattitude and longitude */
    public void getCurrentLatLang() {
        mLatitiude = String.valueOf(gpstrack.getLatitude());
        mLongiitude = String.valueOf(gpstrack.getLongitude());
        LatLng latlng = new LatLng(gpstrack.getLatitude(), gpstrack.getLongitude());
        CameraUpdate center = CameraUpdateFactory.newLatLng(latlng);
        mGoogleMap.moveCamera(center);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
        mGoogleMap.animateCamera(zoom);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);


    }

    /* search api call*/
    private void callApi(SearchInput searchInput) {

        ((MainActivity) getActivity()).mHeaderStarImg.setImageResource(R.drawable.star_header_disable);
        ((MainActivity) getActivity()).mHeaderStarImg.setTag(getString(R.string.get_favorite));
        searchInput.setLatitude(searchInput.getLatitude());
        searchInput.setLongitude(searchInput.getLongitude());
        searchInput.setCity(searchInput.getCity());
        searchInput.setState(searchInput.getState());
        searchInput.setZip(searchInput.getZip());
        searchInput.setName("");
        searchInput.setRadius(searchInput.getRadius());

        if (GlobalMethods.isNetworkAvailable(getActivity())) {

            Bundle params = new Bundle();
            params.putString("zip", searchInput.getZip());
            params.putString("city", searchInput.getCity());
            params.putString("state", searchInput.getState());
            mFirebaseAnalytics.logEvent("Specialist_Searched", params);

            APIRequestHandler.getInstance().findSpecialist(searchInput, this);
        } else {
            DialogManager.showErrorDiag(getActivity(), getString(R.string.internet_error), false);
        }


    }

    /*add markers in the map depends on latitude and longitude*/
    private void addMarkers(ArrayList<SearchEntity> markersList, GoogleMap mMap) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();
            builder = new LatLngBounds.Builder();
        }
        if (markersList.size() > 0) {
            for (int i = 0; i < markersList.size(); i++) {
                try {
                    LatLng latLng = new LatLng(Double.parseDouble(markersList.get(i).getLatitude() != "" ? markersList.get(i).getLatitude() : "0.0"),
                            Double.parseDouble(markersList.get(i).getLongitude() != "" ? markersList.get(i).getLongitude() : "0.0"));
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng);


                    if (mGoogleMap != null) {
                        drawMarker(new LatLng(Double.parseDouble(markersList.get(i).getLatitude()), Double.parseDouble(markersList.get(i).getLongitude())), BitmapDescriptorFactory
                                .fromBitmap(drawTextToBitmap(R.drawable.location_pointer_without_number, String.valueOf(i + 1))), markersList.get(i));

                        if (isMarkerClickForAll) {
                            bounds = builder.build();
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 70);
                            //mMap.setPadding(30, 30, 30, 30);
                            mMap.animateCamera(cu);

//                            if (areBoundsTooSmall(bounds, 300)) {
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 17));
//                            } else {
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
//                            }

                        } else {
                            LatLng moveloc = new LatLng(Double.parseDouble(markersList.get(0).getLatitude() != "" ? markersList.get(0).getLatitude() : "0.0"),
                                    Double.parseDouble(markersList.get(0).getLongitude() != "" ? markersList.get(0).getLongitude() : "0.0"));
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(moveloc, 15));
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

/* marker click action */
            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    SearchEntity searchEntity = (SearchEntity) marker.getTag();
                    if (searchEntity != null) {
                        if (isMarkerClickForAll) {
                            for (int i = 0; i < mLocalSearchFavList.size(); i++) {
                                if (searchEntity.getGuid().equals(mLocalSearchFavList.get(i).getGuid())) {
                                    mRecyclerView.smoothScrollToPosition(i);
                                    break;
                                }
                            }
                        } else {
                            for (int i = 0; i < mSearchList.size(); i++) {
                                if (searchEntity.getGuid().equals(mSearchList.get(i).getGuid())) {
                                    mRecyclerView.smoothScrollToPosition(i);
                                    break;
                                }
                            }
                        }
                        System.out.println("Position " + searchEntity.getFirstname());

                    }
                    return false;
                }
            });
        }
    }

    /* adding markers in the map based on the location search */
    private void drawMarker(LatLng point, BitmapDescriptor bitmapicon, SearchEntity searchEntity) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point).icon(bitmapicon);
        mGoogleMap.addMarker(markerOptions).setTag(searchEntity);
        m_currentMarker = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(getActivity()));
        m_locMarkersHashmap.put(m_currentMarker, searchEntity);

        builder.include(markerOptions.getPosition());

    }
    /*to check for API version*/

    private boolean addPermissions() {
        boolean addPermission = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            addPermission = permissionsAccessLocation();
        }
        return addPermission;
    }

    /*Ask for Permission on location access*/
    private boolean permissionsAccessLocation() {
        boolean addPermission = true;
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permissionLocation = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionCoarseLocation = getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            addPermission = isPermission(listPermissionsNeeded, new InterfaceBtnCallback() {
                @Override
                public void onOkClick() {
/* Further process will happen, if allowed for API version 23 and above */
                    gpstrack = new GPSTracker(getActivity());
                    getCurrentLatLang();
                    searchInput = new SearchInput();
                    searchInput.setLatitude(mLatitiude);
                    searchInput.setLongitude(mLongiitude);


                    callApi(searchInput);


                }
            });
        }

        return addPermission;
    }


    /*set count value on map markers*/
    public Bitmap drawTextToBitmap(int gResId, String gText) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), gResId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getActivity(), 11));

        Rect textRect = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getActivity(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 3) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(gText, xPos, yPos, paint);

        return bm;
    }

    /* Converting the Text size in pixels */
    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);

    }


    /*respected search will be visible by clicking on a particular marker */
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        Context context;

        public MarkerInfoWindowAdapter(Context ctx) {

            context = ctx;

        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.map_info_window, null);

            SearchEntity myMarker = m_locMarkersHashmap.get(marker);
            if (myMarker != null) {

                TextView doctorTxt = (TextView) v.findViewById(R.id.doctor_name_txt);
                doctorTxt.setText(getString(R.string.doctor) + myMarker.getFirstname() + " " +
                        myMarker.getLastname());

            }

            return v;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }


    /* to get the list of doctors added as favorites in Database */
    private void getFavdoctor() {
        mLocalSearchFavList.clear();
        ArrayList<DoctorFavoriteModel> mLocalFavList = db.getAllFavorites();
        if (mLocalFavList.size() > 0) {
            for (int i = 0; i < mLocalFavList.size(); i++) {
                SearchEntity searchEntity = new SearchEntity();
                searchEntity.setGuid(mLocalFavList.get(i).getGuid());
                searchEntity.setFirstname(mLocalFavList.get(i).getFirstname());
                searchEntity.setLastname(mLocalFavList.get(i).getLastname());
                searchEntity.setDegree(mLocalFavList.get(i).getDegree());
                searchEntity.setSpecialty(mLocalFavList.get(i).getSpecialty());
                searchEntity.setAddress1(mLocalFavList.get(i).getAddress());
                searchEntity.setAddress2(mLocalFavList.get(i).getAddress2());
                searchEntity.setCity(mLocalFavList.get(i).getCity());
                searchEntity.setState(mLocalFavList.get(i).getState());
                searchEntity.setZip(mLocalFavList.get(i).getZip());
                searchEntity.setLocation_name(mLocalFavList.get(i).getLocation_name());
                searchEntity.setLatitude(mLocalFavList.get(i).getLatitude());
                searchEntity.setLongitude(mLocalFavList.get(i).getLongitude());
//                searchEntity.setDistance(getDistanceCalculation(mLocalFavList.get(i).getLatitude(),
//                        mLocalFavList.get(i).getLongitude()));
                searchEntity.setDistance(mLocalFavList.get(i).getDistance());
                searchEntity.setPhone_number(mLocalFavList.get(i).getPhonenumber());
                searchEntity.setEmail(mLocalFavList.get(i).getEmail());

                mLocalSearchFavList.add(searchEntity);
            }
        }

    }

    /* calculating the distance between the user and doctors added in the favourite list*/
    private String getDistanceCalculation(String latitude, String longitude) {

        try {
            Location loc1 = new Location("");
            loc1.setLatitude(Double.parseDouble(mLatitiude));
            loc1.setLongitude(Double.parseDouble(mLatitiude));

            Location loc2 = new Location("");
            loc2.setLatitude(Double.parseDouble(latitude));
            loc2.setLongitude(Double.parseDouble(longitude));

            float distanceInMeters = loc1.distanceTo(loc2);
            Double miles = distanceInMeters / 1609.344;
            Log.d("Distance", "" + miles);
            return String.valueOf(miles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


//    private boolean areBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
//        float[] result = new float[1];
//        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
//        return result[0] < minDistanceInMeter;
//    }

    @Override
    public void onResume() {
        super.onResume();
        // AppController.getInstance().trackScreenView(getString(R.string.find_hcv_screen));
        trackScreenName("Find an HCV Specialist");

    }


}
