package com.tb.comment.template;

public interface IContentListener {
    void showName(String name);
    void showFavor(String favor);
    void showProductTitle(String title);
    void showProductImage(String imageUrl);
    void showProductPrice(String price);
    void showProductDiscountPrice(String discountprice);
    void showBackground(String url);
}
