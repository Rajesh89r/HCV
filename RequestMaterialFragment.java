package com.purohit.rethink_hcv.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.purohit.rethink_hcv.R;
import com.purohit.rethink_hcv.activity.MainActivity;
import com.purohit.rethink_hcv.adapters.ResourcePatientsAdpater;
import com.purohit.rethink_hcv.adapters.ResourceProviderAdapter;
import com.purohit.rethink_hcv.entity.ResourceProviderEntity;
import com.purohit.rethink_hcv.mainBase.BaseFragment;
import com.purohit.rethink_hcv.utils.AppConstants;
import com.purohit.rethink_hcv.utils.DialogManager;
import com.purohit.rethink_hcv.utils.GlobalMethods;
import com.purohit.rethink_hcv.utils.WhereFromRequestMaterial;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.purohit.rethink_hcv.utils.AppConstants.mResourcProviderEntityList;
import static com.purohit.rethink_hcv.utils.AppConstants.mResourcePatientEntityList;
import static com.purohit.rethink_hcv.utils.AppConstants.mSelResourcProviderEntityList;
import static com.purohit.rethink_hcv.utils.DialogManager.showConfirmPlacingReq;
import static com.purohit.rethink_hcv.utils.DialogManager.showMaterialListPopup;
import static com.purohit.rethink_hcv.utils.GlobalMethods.getCartList;
import static com.purohit.rethink_hcv.utils.GlobalMethods.storeInToCart;


/**
 * This class implements UI and functions for Instructions
 */
public class RequestMaterialFragment extends BaseFragment {
    ResourcePatientsAdpater resourcePatientsAdpater;
    ResourceProviderAdapter resourceProviderAdapter;
    /*Imageview declaration*/
    @BindView(R.id.resource_provider_recycle)
    RecyclerView mProviderRecycler;
    @BindView(R.id.resource_patient_recycle)
    RecyclerView mPatientRecycler;
    @BindView(R.id.last_material_text)
    TextView mLastMaterialOrderDateTxt;
    @BindView(R.id.count_text)
    public TextView mCountTxt;
    private String[] m_strArrayProviderItem;
    private String[] m_strArrayProviderDetailItem;
    private String[] m_strProviderProductID;
    private String[] m_strPatientsProductID;

    private TypedArray m_typedArrayProviderItemImages;
    ArrayList<TypedArray> m_providerImageList;

    ArrayList<TypedArray> m_patientImageList;


    private String[] m_strArrayPatientItem;
    private String[] m_strArrayProviderItemDesc;
    private String[] m_strArrayProviderItemQty;

    private String[] m_strArrayPatientItemDesc;
    private String[] m_strArrayPatientItemQty;
    private String[] m_strArrayPatientDetailItem;

    private TypedArray m_typedArrayPatientItemImages;
    private TypedArray m_typedArrayPatientBannerImages;

    private GridLayoutManager m_gridLayProvider;
    RequestMaterialFragment reqFragment;

    private Dialog mDialog;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.frag_request_material_screen, container, false);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, mRootView);
        reqFragment = this;
        initview();
        addDummyValues();


        return mRootView;


    }

    private void addDummyValues() {
        //add list for provider from string
        m_providerImageList = new ArrayList<TypedArray>();
        m_patientImageList = new ArrayList<TypedArray>();

        m_strArrayProviderItem = getResources().getStringArray(R.array.request_material_detail_provider_items_array);
        m_typedArrayProviderItemImages = getResources().obtainTypedArray(R.array.request_material_provider_images_array);
        m_strArrayProviderItemDesc = getResources().getStringArray(R.array.request_material_provider_items_desc_array);
        m_strArrayProviderItemQty = getResources().getStringArray(R.array.request_material_provider_items_desc_qty_array);
        m_strArrayProviderDetailItem = getResources().getStringArray(R.array.request_material_detail_provider_items_array);
        m_strProviderProductID = getResources().getStringArray(R.array.provider_product_id);
        //add list for patient from string
        m_strArrayPatientItem = getResources().getStringArray(R.array.request_material_patients_items_array);
        m_typedArrayPatientItemImages = getResources().obtainTypedArray(R.array.request_material_patients_images_array);
        m_strArrayPatientItemDesc = getResources().getStringArray(R.array.request_material_patients_items_desc_array);
        m_strArrayPatientItemQty = getResources().getStringArray(R.array.request_material_patients_items_desc_qty_array);
        m_strArrayPatientDetailItem = getResources().getStringArray(R.array.request_material_patients_detail_items_array);
        m_strPatientsProductID = getResources().getStringArray(R.array.patients_product_id);

        //add image list for provider
        m_providerImageList.add(getResources().obtainTypedArray(R.array.hcv_toolkit_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.baby_boomer_brochure_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.hcv_statistic_brochure_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.liver_progression_chart_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.hcv_screening_guide_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.hcv_brochure_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.risk_factor_sub_array));
        m_providerImageList.add(getResources().obtainTypedArray(R.array.risk_factor_flashcard_sub_array));

        //add image list for patient
        m_patientImageList.add(getResources().obtainTypedArray(R.array.patient_counseling_tool_english_sub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.patient_counseling_tool_spainsh_sub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.waiting_room_checklist_sub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.waiting_room_checklist_spanishsub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.hep_c_resource_english_center_sub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.hep_c_resource_spanish_center_sub_array));
        m_patientImageList.add(getResources().obtainTypedArray(R.array.liver_progression_tear_pad_sub_array));

        mResourcProviderEntityList.clear();
        for (int i = 0; i < m_strArrayProviderItem.length; i++) {
            ResourceProviderEntity res1 = new ResourceProviderEntity();
            res1.setId(m_strProviderProductID[i]);
            res1.setDetailedName(m_strArrayProviderDetailItem[i]);
            res1.setDescription(GlobalMethods.fromHtml(m_strArrayProviderItemDesc[i]) + "");
            res1.setQtyText(m_strArrayProviderItemQty[i] + "");
            res1.setImage(m_typedArrayProviderItemImages.getResourceId(i, -1));
            res1.setName(m_strArrayProviderItem[i]);
//            res1.setBanner_image(m_typedArrayProviderBannerImages.getResourceId(i, -1));
            res1.setImageList(m_providerImageList.get(i));

            mResourcProviderEntityList.add(res1);

        }

        mResourcePatientEntityList.clear();
        for (int i = 0; i < m_strArrayPatientItem.length; i++) {
            ResourceProviderEntity res2 = new ResourceProviderEntity();
            res2.setId(m_strPatientsProductID[i]);
            res2.setDetailedName(m_strArrayPatientDetailItem[i]);
            res2.setDescription(GlobalMethods.fromHtml(m_strArrayPatientItemDesc[i]) + "");
            res2.setQtyText(m_strArrayPatientItemQty[i] + "");
            res2.setImage(m_typedArrayPatientItemImages.getResourceId(i, -1));
            res2.setName(m_strArrayPatientItem[i]);
//            res2.setBanner_image(m_typedArrayPatientBannerImages.getResourceId(i, -1));
            res2.setImageList(m_patientImageList.get(i));

            mResourcePatientEntityList.add(res2);

        }


        resourceProviderAdapter = new ResourceProviderAdapter(mActivity, mResourcProviderEntityList, reqFragment);
        mProviderRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mProviderRecycler.setAdapter(resourceProviderAdapter);
        mProviderRecycler.setNestedScrollingEnabled(false);
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mProviderRecycler.setLayoutParams(params);

        resourcePatientsAdpater = new ResourcePatientsAdpater(mActivity, mResourcePatientEntityList, reqFragment);
        mPatientRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));


        mPatientRecycler.setLayoutParams(params);

        mPatientRecycler.setAdapter(resourcePatientsAdpater);
        mPatientRecycler.setNestedScrollingEnabled(false);

        mLastMaterialOrderDateTxt.setText(mActivity.getResources().getString(R.string.last_material_submited_date) + " " + "01/02/2017");
        mLastMaterialOrderDateTxt.setVisibility(View.GONE);


        /*getting bundle value*/
        if (AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.SERIOUS_IMPACT)
                || AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.WHO_TO_SCREEN)) {
            String cartID = getArguments().getString("CartID");
            for (int i = 0; i < mResourcProviderEntityList.size(); i++) {
                if (cartID.equals(mResourcProviderEntityList.get(i).getId())) {

                    showMsgPopup(mActivity, mResourcProviderEntityList.get(i), 1);

                    break;
                }


            }

        }


    }


    private void initview() {
          /*initalize firebase analytics*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        /* Set header title */
        if (!MainActivity.tabletSize) {
            ((MainActivity) getActivity()).mHeaderLeftImg.setImageResource(AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.HOME_OR_MENU) ?
                    R.drawable.menu_icon : R.drawable.left_arrow_white);
        } else {
            if (!AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.HOME_OR_MENU)) {
                ((MainActivity) getActivity()).mHeaderLeftImg.setVisibility(View.VISIBLE);
            }
        }


        if (mSelResourcProviderEntityList != null) {
            mSelResourcProviderEntityList.clear();
        } else {
            mSelResourcProviderEntityList = new ArrayList<>();
        }

        if (getCartList(getActivity()) != null) {
            mSelResourcProviderEntityList = getCartList(getActivity());
        }
        if (mSelResourcProviderEntityList.size() > 0) {
            ((MainActivity) getActivity()).mCartCountLay.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).mCartCountTxt.setVisibility(View.VISIBLE);
            mCountTxt.setVisibility(View.VISIBLE);

            mCountTxt.setText(String.valueOf(mSelResourcProviderEntityList.size()));

            /*show confirmation alert only shows its came from menu or home*/
//            if (AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.HOME_OR_MENU)) {
//                showConfirmPlacingReq(getActivity());
//            }
            AppConstants.FOR_PLACING_REQUEST = AppConstants.KEY_TRUE;
            ((MainActivity) getActivity()).mCartCountTxt.setText(String.valueOf(mSelResourcProviderEntityList.size()));
        } else {
            mCountTxt.setVisibility(View.GONE);
            ((MainActivity) getActivity()).mCartCountTxt.setVisibility(View.GONE);

        }
        ((MainActivity) getActivity()).mCartCountLay.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).setTitleTxt(getString(R.string.request_material));

        /*click action for header cart_count lay*/
        ((MainActivity) getActivity()).mCartCountLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DialogManager.showErrorDiag(getActivity(), getString(R.string.service_unavailable), false);
                if (mSelResourcProviderEntityList.size() > 0) {
                    //showConfirmPlacingReq(getActivity());
                    showMaterialListPopup(getActivity());
                }
            }
        });


        if (!AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.HOME_OR_MENU)) {
//        /*set back  icon instead of hamburger if its came from serious or who to screen*/
            ((MainActivity) getActivity()).mHeaderLeftImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppConstants.BACK_FROM_REQ_MATERIAL = AppConstants.KEY_TRUE;
                    if (AppConstants.REQMATERIAL_TAG.equals(WhereFromRequestMaterial.SERIOUS_IMPACT)) {

                        mSelResourcProviderEntityList = getCartList(getActivity());
                        if (mSelResourcProviderEntityList != null && mSelResourcProviderEntityList.size() > 0) {
                            AppConstants.VIEW_ID = R.id.serious_impact_menu_lay;
                            showConfirmPlacingReq(getActivity(), true);
                        } else {
                            ((MainActivity) getActivity()).replaceFragment(new SwipeAllFragments());
                        }


                    } else {

                        if (mSelResourcProviderEntityList != null && mSelResourcProviderEntityList.size() > 0) {
                            AppConstants.VIEW_ID = R.id.who_to_menu_lay;
                            showConfirmPlacingReq(getActivity(), true);
                        } else {
                            ((MainActivity) getActivity()).replaceFragment(new SwipeAllFragments());
                        }
                    }


                }
            });
        }
    }


    /*item details popup*/
    public void showMsgPopup(final Context mContext, final ResourceProviderEntity cartItem, final int flag) {
        try {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDialog = DialogManager.getDialog(mContext, R.layout.popup_add_cart_item);
        final ViewPager viewPager = (ViewPager) mDialog.findViewById(R.id.pager);
        Button m_btnOk = (Button) mDialog.findViewById(R.id.submitbutton);
        TextView mTitte = (TextView) mDialog.findViewById(R.id.titletextview);
        TextView mText = (TextView) mDialog.findViewById(R.id.text_view);
        TextView mQtyText = (TextView) mDialog.findViewById(R.id.qty_text);
        ImageView m_btnClose = (ImageView) mDialog.findViewById(R.id.cancel_btn);
//        m_BannerImg.setImageResource(cartItem.getBanner_image());
        final TypedArray m_cartSubImages = cartItem.getImageList();
//        if (m_cartSubImages.length() > 0) {
//            m_BannerImg.setImageResource(m_cartSubImages.getResourceId(0, -1));
//
//        }
//

        viewPager.setAdapter(new ItemDetailPager(getActivity(), cartItem.getImageList()));
        viewPager.setCurrentItem(0);


        if (mSelResourcProviderEntityList != null) {


            if (mSelResourcProviderEntityList.contains(cartItem.getId())) {
                m_btnOk.setText(mContext.getString(R.string.remove_cart_item));

            } else {
                m_btnOk.setText(mContext.getString(R.string.add_cart_item));
            }
        } else {
            m_btnOk.setText(mContext.getString(R.string.add_cart_item));
        }

        if (flag == 2) {
            mTitte.setTextColor(ContextCompat.getColor(mContext, R.color.dark_brown));

        } else {

            mTitte.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
        }
        mQtyText.setText(mContext.getString(R.string.qty) + ": " + cartItem.getQtyText());
        mTitte.setText(cartItem.getDetailedName());
        mText.setText(cartItem.getDescription());
        LinearLayout hori_lay = (LinearLayout) mDialog.findViewById(R.id.horizontal_layout);

        LinearLayout.LayoutParams layoutParams = null;
        if (MainActivity.tabletSize) {
            layoutParams =
                    new LinearLayout.LayoutParams(mContext.getResources().getDimensionPixelOffset(R.dimen.size40),
                            mContext.getResources().getDimensionPixelOffset(R.dimen.size40));
        } else {
            layoutParams =
                    new LinearLayout.LayoutParams(mContext.getResources().getDimensionPixelOffset(R.dimen.size65),
                            mContext.getResources().getDimensionPixelOffset(R.dimen.size65));
        }

        layoutParams.setMargins(10, 10, 10, 10);
        layoutParams.gravity = Gravity.CENTER;

        if (m_cartSubImages.length() > 1) {
            for (int i = 0; i < m_cartSubImages.length(); i++) {
                ImageView iv = new ImageView(mContext);

                iv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                //iv.setPadding(10,10,10,10);
                //iv.setImageResource();
//                Glide.with(mContext).load(m_cartSubImages.getResourceId(i, -1)).placeholder(m_cartSubImages.getResourceId(i, -1)).centerCrop().into(iv);
                Glide.with(mContext).load(m_cartSubImages.getResourceId(i, -1)).asBitmap().override(350, 250).centerCrop().into(iv);
                iv.setLayoutParams(layoutParams);
                iv.setTag(i);
                hori_lay.addView(iv);

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override()
                    public void onClick(View view) {
                        int pos = (int) view.getTag();
                        viewPager.setCurrentItem(pos);
                        //m_BannerImg.setImageResource(m_cartSubImages.getResourceId(pos, -1));
                    }
                });

            }
        } else {
            View view = new View(mContext);
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
            view.setLayoutParams(layoutParams);
            hori_lay.addView(view);
        }


        m_btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        m_btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();

                if (mSelResourcProviderEntityList == null) {
                    mSelResourcProviderEntityList.add(cartItem.getId());


                    addRemoveTrack(true, cartItem.getId(), cartItem.getName());


                } else {
                    if (!mSelResourcProviderEntityList.contains(cartItem.getId())) {
                        mSelResourcProviderEntityList.add(cartItem.getId());

                        addRemoveTrack(true, cartItem.getId(), cartItem.getName());


                    } else {
                        mSelResourcProviderEntityList.remove(cartItem.getId());

                        addRemoveTrack(false, cartItem.getId(), cartItem.getName());
                    }
                }
                storeInToCart(getActivity(), mSelResourcProviderEntityList);


                if (flag == 2) {
                    resourcePatientsAdpater.notifyDataSetChanged();
                } else {
                    resourceProviderAdapter.notifyDataSetChanged();
                }


                mCountTxt.setText(String.valueOf(mSelResourcProviderEntityList.size()));
                if (mSelResourcProviderEntityList.size() > 0) {
                    //((MainActivity) mContext).mCartCountLay.setVisibility(View.VISIBLE);
                    mCountTxt.setVisibility(View.VISIBLE);
                    ((MainActivity) mContext).mCartCountTxt.setVisibility(View.VISIBLE);

                    ((MainActivity) mContext).mCartCountTxt.setText(String.valueOf(mSelResourcProviderEntityList.size()));
                } else {
                    mCountTxt.setVisibility(View.GONE);
                    //  ((MainActivity) mContext).mCartCountLay.setVisibility(View.GONE);
                    ((MainActivity) mContext).mCartCountTxt.setVisibility(View.GONE);

                }

            }
        });
        try {
            if (mDialog != null) {
                mDialog.show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /*Item details slider for left and right swipe*/
    private class ItemDetailPager extends PagerAdapter {
        private Context mContext;
        TypedArray m_cartSubImages;
        LayoutInflater mLayoutInflater;

        public ItemDetailPager(Context context, TypedArray imageList) {
            //Default construction
            mContext = context;
            m_cartSubImages = imageList;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return m_cartSubImages.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View itemView = mLayoutInflater.inflate(R.layout.item_details_view, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.item_img);
            imageView.setImageResource(m_cartSubImages.getResourceId(position, -1));

            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //Parent layout is LinearLayout
            container.removeView((LinearLayout) object);
        }
    }


    @OnClick({R.id.submit_lay})
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.submit_lay:
                if (mSelResourcProviderEntityList.size() > 0) {
                    // DialogManager.showConfirmPlacingReq(getActivity());
                    trackScreenName(getString(R.string.shipping_info_screen));
                    showMaterialListPopup(getActivity());

                }
                //DialogManager.showErrorDiag(getActivity(), getString(R.string.service_unavailable), false);


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        trackScreenName(getString(R.string.req_material_screen));

    }


    public void addRemoveTrack(boolean b, String id, String name) {
        Bundle params = new Bundle();
        params.putString("ProductID", id);
        params.putString("ProductName", name);
        mFirebaseAnalytics.logEvent(b ? "Product_Added" : "Product_Removed", params);


    }


}
