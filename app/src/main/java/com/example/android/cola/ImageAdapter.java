package com.example.android.cola;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by KM on 16. 9. 8..
 */
public class ImageAdapter extends BaseAdapter {
    int CustomGalleryItemBg;
    String mBasePath;
    Context mContext;
    String[] mImgList;
    Bitmap bm;
    DataSetObservable mDataSetObservable = new DataSetObservable(); // DataSetObservable(DataSetObserver)의 생성

    public String TAG = "Gallery Adapter Example :: ";

    public ImageAdapter(Context context, String basepath){
        this.mContext = context;
        this.mBasePath = basepath;

        File file = new File(mBasePath);
        if(!file.exists()){
            if(!file.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }
        }
        mImgList = file.list();

        //TypedArray array = mContext.obtainStyledAttributes(R.styleable);
        //CustomGalleryItemBg = array.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);
        //array.recycle();
    }

    @Override
    public int getCount() {
        File dir = new File(mBasePath);
        mImgList = dir.list();
        return mImgList.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    // Adapter 내 Item에서 직접 주소를 받아오도록 method 추가.
    // 이전에는 MainActivity와 주소 및 position이 달라 비정상적인 앱의 종료가 발생한 것으로 보인다
    public String getItemPath(int position){
        String path = mBasePath + File.separator + mImgList[position];
        return path;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        bm = BitmapFactory.decodeFile(mBasePath + File.separator + mImgList[position]);
        Bitmap mThumbnail = ThumbnailUtils.extractThumbnail(bm, 300, 300);
        imageView.setPadding(8, 8, 8, 8);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
        imageView.setImageBitmap(mThumbnail);
        return imageView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer){ // DataSetObserver의 등록(연결)
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer){ // DataSetObserver의 해제
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged(){ // 위에서 연결된 DataSetObserver를 통한 변경 확인
        mDataSetObservable.notifyChanged();
    }
}
