package com.purohit.rethink_hcv.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.purohit.rethink_hcv.R;
import com.purohit.rethink_hcv.activity.MainActivity;
import com.purohit.rethink_hcv.mainBase.AppController;
import com.purohit.rethink_hcv.mainBase.BaseFragment;
import com.purohit.rethink_hcv.utils.AppConstants;
import com.purohit.rethink_hcv.utils.DialogManager;
import com.purohit.rethink_hcv.utils.ReferenceID;
import com.purohit.rethink_hcv.utils.WhereFromRequestMaterial;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.purohit.rethink_hcv.R.id.hcv_cured_previous_lay_click;
import static com.purohit.rethink_hcv.R.id.serious_next_lay_click;
import static com.purohit.rethink_hcv.utils.DialogManager.getDialog;


/**
 * Created by Red Foundry on 7/13/2017.
 */
public class SwipeAllFragments extends BaseFragment {

    /*pager for view all screens*/
    //@BindView(R.id.pager)
    public static ViewPager mViewPager;
    public MyPageAdapter myPageAdapter;
    List<BaseFragment> mFragmentList;
    SparseArray<Fragment> registeredFragments = new SparseArray<>();


    //Reference button object
    @BindView(R.id.reference_btn)
    Button mReferenceBtn;


    //page Indicator ids
    @BindView(R.id.indicator_one)
    ImageView mImgindicatorone;

    @BindView(R.id.indicator_two)
    ImageView mImgindicatortwo;

    @BindView(R.id.indicator_three)
    ImageView mImgindicatorthree;

    @BindView(R.id.indicator_four)
    ImageView mImgindicatorfour;

    @BindView(R.id.hcv_cured_indicator_one)
    ImageView mHcvCureImageIndicatorOne;

    @BindView(R.id.hcv_cured_indicator_two)
    ImageView mHcvCureImageIndicatorTwo;

    @BindView(R.id.who_dot_indicator_one)
    ImageView mWhoImageIndicatorOne;

    @BindView(R.id.who_dot_indicator_two)
    ImageView mWhoImageIndicatorTwo;
    @BindView(R.id.who_dot_indicator_three)
    ImageView mWhoImageIndicatorThree;

    @BindView(R.id.who_dot_indicator_four)
    ImageView mWhoImageIndicatorFour;


    /*Bottom layout ids*/
    @BindView(R.id.serious_bottom_lay)
    LinearLayout mBottomLay;
    @BindView(R.id.hcv_cured_bottom_lay)
    LinearLayout mHcvCureBottomLay;
    @BindView(R.id.who_bottom_lay)
    LinearLayout mWhoBottomLay;


    /*Next and Previous flow click*/
    @BindView(R.id.serious_next_lay_click)
    LinearLayout mSeriousNextlayClick;
    @BindView(R.id.hcv_cured_previous_lay_click)
    LinearLayout mHCVCurePrevlayClick;
    @BindView(R.id.hcv_cured_next_lay_click)
    LinearLayout mHCVCureNextlayClick;
    @BindView(R.id.who_previous_lay_click)
    LinearLayout mWhoPrevlayClick;
    @BindView(R.id.who_next_lay_click)
    LinearLayout mWhoNextlayClick;

    private int referStrposition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.frag_swipe_all_screen, container, false);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, mRootView);

        initview();

        return mRootView;
    }


    /*initialize the views*/
    private void initview() {

        /* Set header title */
        ((MainActivity) getActivity()).setTitleTxt(getString(R.string.header_serious_impact));

        mFragmentList = new ArrayList<BaseFragment>();


        mFragmentList.add(new SeriousImpactOfHCV1());
        mFragmentList.add(new SeriousImpactOfHCV2());
        mFragmentList.add(new SeriousImpactOfHCV3());
        mFragmentList.add(new SeriousImpactOfHCV4());
        mFragmentList.add(new HCVcuredFragment1());
        mFragmentList.add(new HCVcuredFragment2());
        mFragmentList.add(new WhoToScreenForHCV1());
        mFragmentList.add(new WhoToScreenForHCV2());
        mFragmentList.add(new WhoToScreenForHCV3());
        mFragmentList.add(new WhoToScreenForHCV4());
        mFragmentList.add(new HowHCVisDiagnosed());


        /*Initialise pageslider*/

        myPageAdapter = new MyPageAdapter(getActivity().getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(myPageAdapter);
        // mViewPager.setAdapter(new MyPageAdapter(getActivity().getSupportFragmentManager(), mFragmentList));


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //mFragmentList.get(position).setScreenPosition();
            }

            @Override
            public void onPageSelected(final int position) {
                referStrposition = position;
                //check the position for assign the value for referense string
                if (position == 0 || position == 1 || position == 2 || position == 3) {
                        /*Change indicator images*/
                    mBottomLay.setVisibility(View.VISIBLE);
                    mHcvCureBottomLay.setVisibility(View.GONE);
                    mWhoBottomLay.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).setTitleTxt(getString(R.string.header_serious_impact));
                    ((MainActivity) getActivity()).updatemenuSelection(R.id.serious_impact_menu_lay);
                    mImgindicatorone.setImageResource(position == 0 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                    mImgindicatortwo.setImageResource(position == 1 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                    mImgindicatorthree.setImageResource(position == 2 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                    mImgindicatorfour.setImageResource(position == 3 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                }

                mSeriousNextlayClick.setVisibility(position == 3 ? View.VISIBLE : View.INVISIBLE);
                mHCVCurePrevlayClick.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
                mHCVCureNextlayClick.setVisibility(position == 5 ? View.VISIBLE : View.GONE);
                mWhoPrevlayClick.setVisibility(position == 6 ? View.VISIBLE : View.GONE);
                mWhoNextlayClick.setVisibility(position == 9 ? View.VISIBLE : View.GONE);

                mHcvCureImageIndicatorOne.setImageResource(position == 4 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                mHcvCureImageIndicatorTwo.setImageResource(position == 5 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                mWhoImageIndicatorOne.setImageResource(position == 6 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                mWhoImageIndicatorTwo.setImageResource(position == 7 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                mWhoImageIndicatorThree.setImageResource(position == 8 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);
                mWhoImageIndicatorFour.setImageResource(position == 9 ? R.drawable.circle_bg_indicator_on : R.drawable.circle_bg_indicator_off);

                if (position == 4 || position == 5) {
                    ((MainActivity) getActivity()).setTitleTxt(getString(R.string.header_hcv_can_cured));
                    ((MainActivity) getActivity()).updatemenuSelection(R.id.hcv_can_be_menu_lay);
                    mBottomLay.setVisibility(View.GONE);
                    mWhoBottomLay.setVisibility(View.GONE);
                    mHcvCureBottomLay.setVisibility(View.VISIBLE);
                }
                if (position == 6 || position == 7 || position == 8 || position == 9) {
                    ((MainActivity) getActivity()).setTitleTxt(getString(R.string.who_to_screen_hcv));
                    ((MainActivity) getActivity()).updatemenuSelection(R.id.who_to_menu_lay);
                    mBottomLay.setVisibility(View.GONE);
                    mHcvCureBottomLay.setVisibility(View.GONE);
                    mWhoBottomLay.setVisibility(View.VISIBLE);

                }
                if (position == 10) {
                    ((MainActivity) getActivity()).setTitleTxt(getString(R.string.how_hcv_is_dia));
                    ((MainActivity) getActivity()).updatemenuSelection(R.id.how_hcv_is_menu_lay);
                    mBottomLay.setVisibility(View.GONE);
                    mHcvCureBottomLay.setVisibility(View.GONE);
                    mWhoBottomLay.setVisibility(View.GONE);
                }

                switch (position) {
                    case 0:
                        trackScreenName(getString(R.string.serious_impact1_screen));
                        break;
                    case 1:
                        trackScreenName(getString(R.string.serious_impact2_screen));
                        break;
                    case 2:
                        trackScreenName(getString(R.string.serious_impact3_screen));

                        break;
                    case 3:
                        trackScreenName(getString(R.string.serious_impact4_screen));

                        break;
                    case 4:
                        trackScreenName(getString(R.string.hcv_cured1_screen));
                        break;
                    case 5:
                        trackScreenName(getString(R.string.hcv_cured2_screen));
                        break;
                    case 6:
                        trackScreenName(getString(R.string.who_to_screen1_screen));

                        break;
                    case 7:
                        trackScreenName(getString(R.string.who_to_screen2_screen));
                        break;
                    case 8:
                        trackScreenName(getString(R.string.who_to_screen3_screen));
                        break;
                    case 9:
                        trackScreenName(getString(R.string.who_to_screen4_screen));
                        break;
                    case 10:
                        trackScreenName(getString(R.string.how_hcv_diagnose_screen));
                        break;
                    default:
                        break;


                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mReferenceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (referStrposition) {
                    case 0:
                        trackScreenName(getString(R.string.serious_impact1_screen_ref));
                        AppConstants.REFERENSE_TAG = ReferenceID.SIHCV_SC1;
                        break;
                    case 1:
                        trackScreenName(getString(R.string.serious_impact2_screen_ref));
                        AppConstants.REFERENSE_TAG = ReferenceID.SIHCV_SC2;
                        break;
                    case 2:
                        trackScreenName(getString(R.string.serious_impact3_screen_ref));
                        AppConstants.REFERENSE_TAG = ReferenceID.SIHCV_SC3;
                        break;
                    case 3:
                        trackScreenName(getString(R.string.serious_impact4_screen_ref));
                        AppConstants.REFERENSE_TAG = ReferenceID.SIHCV_SC4;
                        break;
                }
                DialogManager.showReferencePopup(getActivity());


            }
        });
        pageRedirect();


         /*set selection if back from request material screen*/
        if (AppConstants.BACK_FROM_REQ_MATERIAL.equals(AppConstants.KEY_TRUE)
                && AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.SERIOUS_IMPACT)) {
            AppConstants.BACK_FROM_REQ_MATERIAL = AppConstants.KEY_FALSE;
            mViewPager.setCurrentItem(3);
        }
         /*set selection if back from request material screen*/
        if (AppConstants.BACK_FROM_REQ_MATERIAL.equals(AppConstants.KEY_TRUE)
                && AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.WHO_TO_SCREEN)) {
            AppConstants.BACK_FROM_REQ_MATERIAL = AppConstants.KEY_FALSE;
            mViewPager.setCurrentItem(8);
        }

    }

    /*Page redirection dependes on menus selection key values*/
    private void pageRedirect() {
        switch (AppConstants.CLICKED_MENU_TAG) {
            case MENU_SERIOUS_IMPACT:
                mViewPager.setCurrentItem(0);
                break;
            case MENU_HCV_CANBE_CURED:
                mViewPager.setCurrentItem(4);
                break;
            case MENU_WHO_TO_SCREEN:
                mViewPager.setCurrentItem(6);
                break;
            case MENU_HOW_HCV_DIAGNOSE:
                mViewPager.setCurrentItem(10);
                break;
            default:
                break;
        }

    }


    /**
     * Custom Page adapter
     */
    private class MyPageAdapter extends FragmentStatePagerAdapter {

        List<BaseFragment> mFragmentList;


        public MyPageAdapter(FragmentManager fragmentManager, List<BaseFragment> mFragmentList) {
            super(fragmentManager);
            this.mFragmentList = mFragmentList;
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SeriousImpactOfHCV1();
                case 1:
                    return new SeriousImpactOfHCV2();
                case 2:
                    return new SeriousImpactOfHCV3();
                case 3:
                    return new SeriousImpactOfHCV4();
                case 4:
                    return new HCVcuredFragment1();
                case 5:
                    return new HCVcuredFragment2();
                case 6:
                    return new WhoToScreenForHCV1();
                case 7:
                    return new WhoToScreenForHCV2();
                case 8:
                    return new WhoToScreenForHCV3();
                case 9:
                    return new WhoToScreenForHCV4();
                case 10:
                    return new HowHCVisDiagnosed();
                default:
                    return new HomeFragment();

            }
        }
    }


    @OnClick({serious_next_lay_click, hcv_cured_previous_lay_click, R.id.hcv_cured_next_lay_click, R.id.who_previous_lay_click,
            R.id.who_next_lay_click})
    public void OnClick(View view) {
        switch (view.getId()) {
            case serious_next_lay_click:
                mViewPager.setCurrentItem(4);
                break;
            case hcv_cured_previous_lay_click:
                mViewPager.setCurrentItem(3);
                break;
            case R.id.hcv_cured_next_lay_click:
                mViewPager.setCurrentItem(6);
                break;
            case R.id.who_previous_lay_click:
                mViewPager.setCurrentItem(5);
                break;
            case R.id.who_next_lay_click:
                mViewPager.setCurrentItem(10);
                break;
        }
    }


    //Reference Pop up
    private void showReferencePopup(Context mContext) {
        final Dialog mDialog = getDialog(mContext, R.layout.reference_popup_alert);
        //intialize reference content text
        TextView referenceTxt;
        ImageView mCloseimg;
        referenceTxt = (TextView) mDialog.findViewById(R.id.content_ref_txt);
        mCloseimg = (ImageView) mDialog.findViewById(R.id.close_img);


        /*popup close */
        mCloseimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        //referenceTxt.setText(Html.fromHtml("1.Yehia BR et al." + "<i>PLoS One.</i>" + "2014;9(4):1-7."));

        referenceTxt.setMovementMethod(LinkMovementMethod.getInstance());
        switch (referStrposition) {
            case 0:
                referenceTxt.setText(mContext.getText(R.string.si_reference_Yehia));
                break;
            case 1:
                referenceTxt.setText(mContext.getText(R.string.si_reference_CDC));
                break;
            case 2:
                referenceTxt.setText(mContext.getText(R.string.si_reference_smith));
                break;
            case 3:
                referenceTxt.setText(mContext.getText(R.string.si_reference_davis));
                break;
        }


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = mDialog.getWindow();
        if (window != null) {
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        //cancel popup outsoside click or touch
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.show();

    }


}

