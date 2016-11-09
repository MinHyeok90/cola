package com.example.android.cola;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/*
 * Created by 김미래 on 2016-11-05
 *  갤러리 이미지 선택 Activity.
 *  선택한 이미지 업로드 할 수 있게 구현
 *  녹화 시작 이후로 찍은 모든 사진 불러옴
 *
 *  TODO: 디자인 수정, 보완
 *  TODO: 선택된 아이템 개수 세기
 *  TODO: 녹화 시작 이전의 사진도 불러올 수 있게 버튼 추가 및 인텐트 연결
 */
public class ImagePickActivity extends AppCompatActivity {
    public final String TAG = "ImagePickActivity";
    public GridView mGridView;
    public ImagePickActivity.GridAdapter gridAdapter;
    public Activity activity = this;
    private Context mContext;

    public final int REQ_CODE_PICK_PICTURE = 335;

    private String mStartDate;
    ArrayList<ImageData> mAlbumList = new ArrayList<ImageData>();
    public boolean mSelectedAll = false;
    public int mSelectedCount = 0;

    public final static int PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);
        mContext = this;
        mStartDate = getIntent().getStringExtra("startDate");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("사진 선택");

        long s = Long.parseLong(mStartDate);
        Date date = new Date(s);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM년 dd일 HH:mm:ss");
        TextView textView = (TextView) findViewById(R.id.imagePickerTextView);
        textView.setText(simpleDateFormat.format(date)+"부터 찍은 모든 사진");

        /* 안드로이드 6.0 이후 내부 저장소 접근하려면 권한 승인 필요요 */
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(ImagePickActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImagePickActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ImagePickActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        mGridView = (GridView) findViewById(R.id.imagePickerGrid);
        gridAdapter = new GridAdapter(mContext, R.layout.gallerygriditem, mAlbumList);
        mGridView.setAdapter(gridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
                final TextView checkView = (TextView) view.findViewById(R.id.galleryImageCheck);
                mAlbumList.get(position).isChecked = !mAlbumList.get(position).isChecked;

                /*if(albumList.get(position).isChecked){
                    imageView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);
                    view.setBackgroundColor(Color.parseColor("#FFFF00"));
                    checkView.setBackgroundColor(Color.WHITE);
                    checkView.setTextColor(Color.GREEN);

                }
                else{
                    imageView.clearColorFilter();
                    view.setBackgroundColor(Color.TRANSPARENT);
                    checkView.setBackgroundColor(Color.TRANSPARENT);
                }*/
                gridAdapter.notifyDataSetChanged();
            }
        });


    }
    public class GridAdapter extends BaseAdapter {
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List arrayList;
        //StorageReference storageReference;
        //private ArrayList<String> thumbsDataList;
        //private ArrayList<String> thumbsIDList;

        public GridAdapter(Context context, int layout, ArrayList<ImageData> arrayList) {
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.arrayList = arrayList;
            //this.storageReference = storageReference;
            //thumbsDataList = new ArrayList<String>();
            //thumbsIDList = new ArrayList<String>();
            getThumbInfo(arrayList);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        /*@Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }*/

        @Override
        public ImageData getItem(int i) {
            return (ImageData) arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
                view = layoutInflater.inflate(layout, null);
            // Put it in the image view

            final ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
            final TextView checkView = (TextView) view.findViewById(R.id.galleryImageCheck);

            Glide.with(context)
                    .load(getItem(i).getThumbsData())
                    .centerCrop()
                    .override(256, 256)
                    .error(R.drawable.ic_action_name)
                    .into(imageView);

            if(getItem(i).isChecked){
                // 이미지 선택 시 색깔, 체크박스 형태 변경경
               imageView.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);
                view.setBackgroundColor(Color.parseColor("#FFFF00"));
                checkView.setBackgroundColor(Color.WHITE);
                checkView.setTextColor(Color.GREEN);
            }
            else {
                imageView.clearColorFilter();
                view.setBackgroundColor(Color.TRANSPARENT);
                checkView.setBackgroundColor(Color.TRANSPARENT);
                checkView.setTextColor(Color.WHITE);
                //checkBox.setChecked(getItem(i).isChecked);
            }
            return view;
        }
        private void getThumbInfo(ArrayList<ImageData> arrayList){
            //최근 파일 불러오기, projection: select할 필드 선택
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.MIME_TYPE
            };
            //long start = new Date((2016 - 1900), 9, 20, 0, 0, 0).getTime();
            long start = Long.parseLong(mStartDate);
            String where = MediaStore.Images.Media.DATE_TAKEN + " >= " + start;

            //문제 있음. 외장메모리 없는 경우 External_content_url 작동 안함
            //Internal_conent_uri로 실행 시 잘 안 되는 것 같음
            Cursor cursor = getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                            null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
            ; //returns cursor with 3 columns mentioned above

            if(cursor != null ) {
                String thumbsID;
                String thumbsData;
                String thumbsDisplayName;
                String thumbsDate;
                String thumbsMimeType;

                int thumbsIDCol = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsDisplayNameCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int thumbsImageDateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int thumbsMimeTypeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

                while (cursor.moveToNext()) {

                    thumbsID = cursor.getString(thumbsIDCol);
                    thumbsData = cursor.getString(thumbsDataCol);
                    thumbsDisplayName = cursor.getString(thumbsDisplayNameCol);
                    thumbsDate = cursor.getString(thumbsImageDateCol);
                    thumbsMimeType = cursor.getString(thumbsMimeTypeCol);

                    //imgSize = cursor.getString(thumbsSizeCol);
                    ImageData imageData = new ImageData(thumbsData, thumbsDate, thumbsDisplayName, thumbsID, thumbsMimeType);
                    if (thumbsID != null) {
                        //thumbsIDs.add(thumbsID);
                        //thumbsDatas.add(thumbsData);
                        arrayList.add(imageData);
                    }
                }
            }
            cursor.close();
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
            };
            Cursor cursor = getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, "_ID='"+ thumbID +"'"/*where*/,
                            null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            if (cursor != null && cursor.moveToFirst()){
                if (cursor.getCount() > 0){
                    int imgData = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = cursor.getString(imgData);
                }
            }
            cursor.close();
            return imageDataPath;
        }

    }
    public class ImageData{
        public String thumbsID;
        public String thumbsData;
        public String thumbsDisplayName;
        public String thumbsDate;
        public String thumbsMimeType;
        public boolean isChecked;

        public ImageData(String thumbsData, String thumbsDate, String thumbsDisplayName, String thumbsID, String thumbsMimeType) {
            this.thumbsData = thumbsData;
            this.thumbsDate = thumbsDate;
            this.thumbsDisplayName = thumbsDisplayName;
            this.thumbsID = thumbsID;
            this.thumbsMimeType = thumbsMimeType;
            this.isChecked = mSelectedAll;
        }

        public String getThumbsData() {
            return thumbsData;
        }

        public void setThumbsData(String thumbsData) {
            this.thumbsData = thumbsData;
        }

        public String getThumbsDate() {
            return thumbsDate;
        }

        public void setThumbsDate(String thumbsDate) {
            this.thumbsDate = thumbsDate;
        }

        public String getThumbsDisplayName() {
            return thumbsDisplayName;
        }

        public void setThumbsDisplayName(String thumbsDisplayName) {
            this.thumbsDisplayName = thumbsDisplayName;
        }

        public String getThumbsID() {
            return thumbsID;
        }

        public void setThumbsID(String thumbsID) {
            this.thumbsID = thumbsID;
        }

        public String getThumbsMimeType() {
            return thumbsMimeType;
        }

        public void setThumbsMimeType(String thumbsMimeType) {
            this.thumbsMimeType = thumbsMimeType;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_imagepick, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder bld;    //대화상자 출력준비.
        switch (item.getItemId()) {
            /* 선택 완료. 업로드 */
            case R.id.action_complete_select:
                Intent intent = new Intent();
                List list = new ArrayList();

                if(mAlbumList.size()==0){
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
                boolean a = true;
                ClipData clipData = null;
                //String[] mimeTypes = new String[]{"image/jpeg"};
                for(int i=0; i<mAlbumList.size(); i++){
                    if(mAlbumList.get(i).isChecked) {
                        if(a){
                            int id = Integer.parseInt(mAlbumList.get(i).getThumbsID());
                            clipData = new ClipData("ImagePickItem", new String[]{"image/jpeg"},
                                    new ClipData.Item(ContentUris.withAppendedId( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id )));
                            a = false;
                        }
                        else {
                            int myid = Integer.parseInt(mAlbumList.get(i).getThumbsID());
                            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, myid);
                            ClipData.Item clipdataItem = new ClipData.Item(uri);
                            clipData.addItem(clipdataItem);
                        }
                    }
                }
                intent.setClipData(clipData);
                //intent.putExtra("key", value);
                setResult(RESULT_OK, intent);

                finish();
                return true;

            /* 모두 선택/해제 */
            case R.id.action_select_all:
                mSelectedAll = !mSelectedAll;
                for(int i=0; i<mAlbumList.size(); i++){
                    mAlbumList.get(i).isChecked = mSelectedAll;
                }
                gridAdapter.notifyDataSetChanged();
                return true;

            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            /* 갤러리에서 선택 */
            case R.id.action_select_gallery:
                Intent i = new Intent(Intent.ACTION_PICK);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // images on the SD card.

                // 결과를 리턴하는 Activity 호출
                startActivityForResult(i, REQ_CODE_PICK_PICTURE);
                return true;
            default:
                return true;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE_PICK_PICTURE) {
                setResult(resultCode, data);
                finish();
            }
        }
    }
}
