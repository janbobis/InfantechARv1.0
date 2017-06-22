package com.infantechar.application;

/**
 * Created by ISABELLA on 5/21/2017.
 */

public enum BitmapEnum {
    DEFAULT_BITMAP("DEFAULT", R.drawable.infantechar),
    CITI_BITMAP("CITI", R.drawable.citi),
    BPI_BITMAP("BPI", R.drawable.bpi),
    NIKE_BITMAP("NIKE", R.drawable.nike),
    ADIDAS_BITMAP("ADIDAS", R.drawable.adidas),
    JOLLIBEE_BITMAP("JOLLIBEE", R.drawable.jollibee),
    MCDONALDS_BITMAP("MCDONALDS", R.drawable.mcdonalds);
    private String keyword;
    private int bitmap;

    BitmapEnum(String keyword, int bitmap){
        this.keyword = keyword;
        this.bitmap = bitmap;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getBitmap() {
        return bitmap;
    }

    public void setBitmap(int bitmap) {
        this.bitmap = bitmap;
    }

    public static BitmapEnum getEnum(String keyword){
        if(null!=keyword) {
            for (BitmapEnum bitmap : BitmapEnum.values()) {
                if (bitmap.getKeyword().equalsIgnoreCase(keyword)) {
                    return bitmap;
                }
            }
        }
        return DEFAULT_BITMAP;
    }
}
