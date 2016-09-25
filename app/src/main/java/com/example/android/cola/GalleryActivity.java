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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class GalleryActivity extends AppCompatActivity {
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("albumtest");
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef =storage.getReferenceFromUrl("gs://cola-b6336.appspot.com");
    StorageReference imagesRef;

    //FirebaseStorage storage = FirebaseStorage.getInstance();
    //StorageReference storageRef = storage.getReferenceFromUrl("gs://<your-bucket-name>");
    private int PICK_IMAGE_REQUEST = 1;
    public final static int PICK_PHOTO_CODE = 1046;
    public final String TAG = "GalleryActivity";
    public String basePath = null;
    public GridView mGridView;
    public GridAdapter gridAdapter;
    public Activity activity = this;

    public final long start = new Date((2016-1900),8,20,0,0,0).getTime();
    //public final long start = ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        String albumName = "1";

        long s = start;
        Date dates = new Date(s);

        DatabaseReference mReference =  myRef.child(albumName).child("filelist");
        final StorageReference albumReference = storageRef.child(albumName);

        //Query query = mReference.orderByChild("filename");
        final List albumList = new ArrayList();

        mGridView = (GridView)findViewById(R.id.gridView);

        mReference.addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    albumList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        //String idx = child.getKey().toString();
                        String fileName = child.child("filename").getValue().toString();
                        albumList.add(fileName);
                    }
                    gridAdapter = new GridAdapter(getApplicationContext(), R.layout.gallerygriditem, albumList, albumReference);
                    mGridView.setAdapter(gridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    // ...
                }
            });
    }

    void onClick(View v){

        switch (v.getId()) {
            /*case R.id.downloadButton:
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
               // Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
                break;
            */
            case R.id.loadButton:
                //최근 파일 불러오기
                String[] projection = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.MIME_TYPE
                };
                String where = MediaStore.Images.Media.DATE_TAKEN +" >= " + start;
                //String where = null;
                Cursor cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");; //returns cursor with 3 columns mentioned above

                String albumName = "1";

                final DatabaseReference mReference =  myRef.child(albumName).child("filelist");
                final StorageReference albumReference = storageRef.child(albumName);
                boolean a = cursor.isAfterLast();
                while(cursor.moveToNext()){
                    String filePath = cursor.getString(1);
                    final String filename = filePath.split("/")[filePath.split("/").length-1];
                    String dateTaken = cursor.getString(3);
                    long dTaken = Long.parseLong(dateTaken);
                    Date date = new Date(dTaken);

                    //System.out.print("");
                    Uri file = Uri.fromFile(new File(filePath));
                    StorageReference fileReference = albumReference.child(filename);

                    UploadTask uploadTask = fileReference.putFile(file);

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

                            mReference.push().child("filename").setValue(filename);
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            TextView progressView = (TextView) findViewById(R.id.progress);
                            double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            System.out.println("Upload is " + progress + "% done");
                            progressView.setText("uploading: "+progress+"%");
                        }
                    });
                }
                break;
        }
    }

    public class GridAdapter extends BaseAdapter{
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List arrayList;
        StorageReference storageReference;

        public GridAdapter(Context context, int layout, List arrayList, StorageReference storageReference){
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.arrayList = arrayList;
            this.storageReference = storageReference;

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
            final StorageReference imagesReference = storageReference.child(getItem(i).toString());
            final long MAX_BYTE = 1024;
            /*imagesReference.getDownloadUrl()*/
            imagesReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    final String mUrl = uri.toString();
                    final Bitmap bitmap;
                    ImageLoadTask imgLoadTask = new ImageLoadTask(mUrl,imageView);
                    imgLoadTask.execute();


                    /*new Thread() {
                        public void run() {

                        }
                    }.start();*/

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d(TAG,"error", exception);
                }
            });


            /*if (view==null)
                view = layoutInflater.inflate(layout, null);
            ImageView imageView = (ImageView)view.findViewById(R.id.galleryImageView);
            imageView.setImageResource(img[i]);*/

            return view;
        }
    }
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


}
