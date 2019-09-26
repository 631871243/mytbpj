package com.tb.comment.TemplateView;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tb.comment.template.IContentListener;

public class TemplateView implements IContentListener {

    private TextView mShopNameView;
    private TextView mProductFavorView;
    private TextView mProductTitleView;
    private TextView mProductPriceView;
    private TextView mProductDiscountPriceView;
    private ImageView mProductImageView;
    private ImageView mBackgroundView;
    private Context context;

    public TemplateView(Context context){
        this.context = context;
    }

    public void setView(TextView mShopNameView,TextView mProductFavorView,TextView mProductTitleView,TextView mProductPriceView,TextView mProductDiscountPriceView,ImageView mProductImageView,ImageView mBackgroundView){
        this.mShopNameView = mShopNameView;
        this.mProductFavorView = mProductFavorView;
        this.mProductTitleView = mProductTitleView;
        this.mProductPriceView = mProductPriceView;
        this.mProductDiscountPriceView = mProductDiscountPriceView;
        this.mProductImageView = mProductImageView;
        this.mBackgroundView = mBackgroundView;
    }

/*    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mShopNameView = (TextView)findViewById(R.id.shop_name);
        mProductFavorView = (TextView)findViewById(R.id.product_favor);
        mProductTitleView = (TextView)findViewById(R.id.product_title);
        mProductPriceView = (TextView)findViewById(R.id.product_price);
        mProductDiscountPriceView = (TextView)findViewById(R.id.product_discount_price);
        mProductImageView = (ImageView)findViewById(R.id.product_img);
        mBackgroundView = (ImageView)findViewById(R.id.bg);
    }*/

    @Override
    public void showName(String name) {
        if(mShopNameView != null){
            mShopNameView.setText(name);
        }
    }

    @Override
    public void showFavor(String favor) {
        if(mProductFavorView != null){
            mProductFavorView.setText(favor);
        }
    }

    @Override
    public void showProductTitle(String title) {
        if(mProductTitleView != null){
            mProductTitleView.setText(title);
        }
    }

    @Override
    public void showProductImage(String imageUrl) {
        Glide.with(context).load(imageUrl).into(mProductImageView);
    }

    @Override
    public void showProductPrice(String price) {
        if(mProductPriceView != null){
            mProductPriceView.setText(price);
        }
    }

    @Override
    public void showProductDiscountPrice(String discountprice) {
        if(mProductDiscountPriceView != null){
            mProductDiscountPriceView.setText(discountprice);
        }
    }

    @Override
    public void showBackground(String url) {
        Glide.with(context).load(url).into(mBackgroundView);
    }
}
