package com.example.android.cola;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("albumtest");
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef =storage.getReferenceFromUrl("gs://cola-b6336.appspot.com");
    StorageReference imagesRef;

    private int PICK_IMAGE_REQUEST = 1;
    public final static int PICK_PHOTO_CODE = 1046;
    public final String TAG = "GalleryActivity";
    public String basePath = null;
    public GridView mGridView;
    public GridAdapter gridAdapter;
    public Activity activity = this;

    public final long start = new Date((2016-1900),9,20,0,0,0).getTime();
    private final List filenameList = new ArrayList();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        String albumKey = "1";// 인텐트에서 받아온 앨범 key 값으로 변경할것
        String albumName = "albumname" ;//인텐트에서 앨범 이름 받아오기
        String startDate = "1476889200000"; //이것도 인텐트에서 날짜 받아오는게..

        DatabaseReference mReference =  myRef.child(albumKey).child("filelist");

        // ActionBar에 타이틀 변경
        getSupportActionBar().setTitle(albumName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long s = Long.parseLong(startDate);
        Date dates = new Date(s);

        final List albumList = new ArrayList();

        mGridView = (GridView)findViewById(R.id.gridView);
        gridAdapter = new GridAdapter(getApplicationContext(), R.layout.gallerygriditem, albumList);
        mGridView.setAdapter(gridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml

        /*
        * filelist 변경될 때마다 호출됨
        */
        mReference.addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    albumList.clear();
                    filenameList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if(child != null) {
                            //Log.d(TAG, "aaaaaaaaaa aaaaaa aaaaa : "+child.toString());
                            /*
                            * db에서 받아온 url, filename 등을 어댑터에 bind된 arrayList에 넣고
                            * 어댑터에 notifyDataSetChanged 해줌
                            */
                            String fileUri = child.child("url").getValue().toString();
                            String fileName = child.child("filename").getValue().toString();
                            albumList.add(fileUri);
                            filenameList.add(fileName);
                        }
                    }
                    gridAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    // ...
                }
            });
    }
    @Override
    public boolean onSupportNavigateUp()
    {
        return super.onSupportNavigateUp();
    }
    void onClick(View v){

        switch (v.getId()) {

            case R.id.loadButton:
                String albumKey = "1";// 인텐트에서 받아온 앨범 key 값으로 변경할것
                final DatabaseReference mReference =  myRef.child(albumKey).child("filelist");
                final StorageReference albumReference = storageRef.child(albumKey);

                //최근 파일 불러오기, projection: select할 필드 선택
                String[] projection = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.MIME_TYPE
                };
                String where = MediaStore.Images.Media.DATE_TAKEN +" >= " + start;

                //문제 있음. 외장메모리 없는 경우 External_content_url 작동 안함
                //Internal_conent_uri로 실행 시 잘 안 되는 것 같음
                Cursor cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");; //returns cursor with 3 columns mentioned above

                boolean a = cursor.isAfterLast();
                while(cursor.moveToNext()){
                    String filePath = cursor.getString(1);
                    final String filename = filePath.split("/")[filePath.split("/").length-1];
                    if(!filenameList.contains(filename)) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        Bitmap src = BitmapFactory.decodeFile( filePath, options );
                        Bitmap resized = Bitmap.createScaledBitmap( src, 256, 256, true );

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitmapData = bos.toByteArray();
                        InputStream bs = new ByteArrayInputStream(bitmapData);

                        String dateTaken = cursor.getString(3);
                        long dTaken = Long.parseLong(dateTaken);
                        Date date = new Date(dTaken);
                        StorageReference fileReference = albumReference.child(filename);

                        UploadTask uploadTask = fileReference.putStream(bs);

                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                DatabaseReference r = mReference.push();
                                String filename = taskSnapshot.getMetadata().getName();
                                r.child("url").setValue(downloadUrl.toString());
                                r.child("filename").setValue(filename);

                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                TextView progressView = (TextView) findViewById(R.id.progress);
                                double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                System.out.println("Upload is " + progress + "% done");
                                progressView.setText("uploading: " + progress + "%");
                            }
                        });
                    }
                }
                break;
        }
    }

    public class GridAdapter extends BaseAdapter{
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List arrayList;
        //StorageReference storageReference;

        public GridAdapter(Context context, int layout, List arrayList){
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.arrayList = arrayList;
            //this.storageReference = storageReference;

        }
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view==null)
                view = layoutInflater.inflate(layout, null);
            // Put it in the image view

            final ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
            final long MAX_BYTE = 1024;

            Glide.with(getApplicationContext())
                    .load(getItem(i))
                    .centerCrop()
                    .override(256,256)
                    .error(R.drawable.ic_action_name)
                    .into(imageView);

            return view;
        }
    }/*
    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();


                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(myBitmap, 240,120);
                return thumbnail;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);

        }

    }
*/

}
