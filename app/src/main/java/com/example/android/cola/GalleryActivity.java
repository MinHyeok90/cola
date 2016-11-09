package com.example.android.cola;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.input;
import static com.google.android.gms.internal.zzaoj.bld;

/*
 * Created by 김미래 on 2016-09-15
 *
 * Modify by 김민혁 on 2016-10-27
 *  album 제목 변경 : UI, 기능 구현
 *  album 나가기 : UI 구현
 *
 * Modified by 김미래 on 2016-11-04
 *  사진 업로드 시 owner 정보도 업로드.
 *  TODO: owner 정보 화면에 출력될 수 있게 할 것.
 *
 * Modify by 김민혁 on 2016-11-08
 *  사진 업로드 시 썸네일이 DEFALUT면 썸네일 Url 변경
 *
 * Modify by 김민혁 on 2016-11-09
 *  앨범 삭제시 storage에서도 사진 모두 삭제
 */

public class GalleryActivity extends AppCompatActivity {
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("albumtest");
    public FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://cola-b6336.appspot.com");
    StorageReference imagesRef;

    public final String TAG = "GalleryActivity";
    public String basePath = null;
    public GridView mGridView;
    public GridAdapter gridAdapter;
    public Activity activity = this;
    public final int REQ_CODE_PICK_PICTURE = 335;
    public FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public final long start = new Date((2016 - 1900), 9, 20, 0, 0, 0).getTime();
    private final List filenameList = new ArrayList();

    /**
     * 2016.10.27. by 김미래
     * 아래 3개 값 전역변수로 변경
     *
     * 2016.11.08. by 김민혁
     * thumnail변수 추가
     */
    private String mAlbumKey  = null;
    private String mAlbumName = null;
    private String mStartDate = null;
    private String mThumbnail = null;
    private String mAlbumOwner = null;

    final List albumList = new ArrayList();

    //0이면 지워져야 하므로
    private int partyCount = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();

        mAlbumKey = intent.getStringExtra("albumKey");// 인텐트에서 받아온 앨범 key 값으로 변경할것? 혹은 db 연
        mAlbumName = intent.getStringExtra("albumName");//인텐트에서 앨범 이름 받아오기
        mStartDate = intent.getStringExtra("albumDate"); //이것도 인텐트에서 날짜 받아오는게..
        mAlbumOwner = intent.getStringExtra("albumOwner"); //이것도 인텐트에서 날짜 받아오는게..

        DatabaseReference mReference = myRef.child(mAlbumKey).child("filelist");

        // ActionBar에 타이틀 변경
        getSupportActionBar().setTitle(mAlbumName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long s = Long.parseLong(mStartDate);
        //Date dates = new Date(s);

        mGridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridAdapter(getApplicationContext(), R.layout.gallerygriditem, albumList);
        mGridView.setAdapter(gridAdapter);  // 커스텀 아답타를 GridView 에 적용// GridView 항목의 레이아웃 row.xml
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uri = albumList.get(position).toString();
                Intent intent = new Intent(activity, DetailActivity.class);

                intent.putExtra("url", uri);
                startActivity(intent);
            }
        });

        //썸네일 정보를 읽기위한 리스너.
        myRef.child(mAlbumKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //썸네일 정보를 읽음. 앨범을 삭제할 경우, 썸네일이 null로 반환되므로 null인지 확인 후 toString()수행.
                Object thumb = dataSnapshot.child("thumbnail").getValue();
                if(thumb != null)
                    mThumbnail = thumb.toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*myRef.child(albumKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        /*
        * filelist 변경될 때마다 호출됨
        */
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                albumList.clear();
                filenameList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child != null) {
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
        } ;
        mReference.addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    void onClick(View v) {

        switch (v.getId()) {

            /*case R.id.loadButton:
                //String albumKey = "1";// 인텐트에서 받아온 앨범 key 값으로 변경할것
                final DatabaseReference mReference = myRef.child(mAlbumKey).child("filelist");
                final StorageReference albumReference = storageRef.child(mAlbumKey);

                //최근 파일 불러오기, projection: select할 필드 선택
                String[] projection = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.MIME_TYPE
                };
                String where = MediaStore.Images.Media.DATE_TAKEN + " >= " + start;

                //문제 있음. 외장메모리 없는 경우 External_content_url 작동 안함
                //Internal_conent_uri로 실행 시 잘 안 되는 것 같음
                Cursor cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
                ; //returns cursor with 3 columns mentioned above

                boolean a = cursor.isAfterLast();
                while (cursor.moveToNext()) {
                    String filePath = cursor.getString(1);
                    final String filename = filePath.split("/")[filePath.split("/").length - 1];
                    if (!filenameList.contains(filename)) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        Bitmap src = BitmapFactory.decodeFile(filePath, options);
                        Bitmap resized = Bitmap.createScaledBitmap(src, 256, 256, true);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.PNG, 0 *//*ignored for PNG*//*, bos);
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
                break;*/
        }
    }

    public class GridAdapter extends BaseAdapter {
        Context context;
        int layout;
        LayoutInflater layoutInflater;
        List arrayList;
        //StorageReference storageReference;

        public GridAdapter(Context context, int layout, List arrayList) {
            this.context = context;
            this.layout = layout;
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            if (view == null)
                view = layoutInflater.inflate(layout, null);
            // Put it in the image view

            final ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
            final long MAX_BYTE = 1024;

            Glide.with(getApplicationContext())
                    .load(getItem(i))
                    .centerCrop()
                    .override(256, 256)
                    .error(R.drawable.ic_action_name)
                    .into(imageView);

            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
        inflater.inflate(R.menu.menu_addpicture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* 메뉴 기능 */
    /*
     * Modify by 김민혁 on 2016-10-27
     *  album 제목 변경 : UI, 기능 구현
     *  album 나가기 : UI 구현
     *
     * Modify by 김민혁 on 2016-11-03
     *  album 참여자들 메뉴추가
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder bld;    //대화상자 출력준비.
        switch (item.getItemId()) {

            case R.id.action_show_participants:
                //action_show_participants버튼 recommit
                Intent i = new Intent(this, AddNewMemberActivity.class);
                i.putExtra("albumkey",mAlbumKey);
                i.putExtra("menu","party");
                startActivity(i);
                //action_show_participants버튼 recommit

                return true;
            case R.id.action_edit_album_title:
                /* 이름변경 버튼 클릭시 */
                /* 대화상자 재료 준비 */
                final EditText input = new EditText(this);
                input.setHint("새로운 앨범 제목을 작성해주세요.");
                input.setText(mAlbumName);                          //현재 제목으로 채워두기.
                input.setSelection(0,input.getText().length());     //전체 선택상태.

                //자동으로 키보드 띄우는 2줄
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                /* 대화상자 생성 시작 */
                bld = new AlertDialog.Builder(this);
                bld.setTitle("앨범 이름 변경");
                bld.setView(input);                                 //EditText 장착
                bld.setIcon(R.drawable.imoticon1);
                bld.setPositiveButton("변경",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /* 변경 시작 */
                        mAlbumName = input.getText().toString();
                        myRef.child(mAlbumKey).child("name").setValue(mAlbumName);

                        // ActionBar에 타이틀 변경
                        getSupportActionBar().setTitle(mAlbumName);
                    }
                });
                bld.setNegativeButton("취소",null);
                bld.show();
                return true;

            case R.id.action_exit_album:
                /* 나가기 버튼 클릭시 */
                /* 대화상자 재료 준비 */
                final TextView output = new TextView(this);
                output.setText("이 앨범을 더 이상 보지 않으시겠습니까?");
                output.setTextSize(24);
                output.setTextColor(Color.RED);
                output.setGravity(Gravity.CENTER);

                /* 대화상자 생성 시작*/
                bld = new AlertDialog.Builder(this);
                bld.setTitle("앨범에서 나가기");
                bld.setView(output);                //TextView 장착
                bld.setIcon(R.drawable.imoticon1);
                bld.setPositiveButton("앨범에서 나가기",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        String owner = myRef.child(mAlbumKey).child("owner");
                        /* 이탈하기 */

                        myRef.child(mAlbumKey).child("participants").child(mUser.getUid()).removeValue();
                        myRef.child(mAlbumKey).child("participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String uid="";
                                partyCount = (int)dataSnapshot.getChildrenCount();
                                Log.i(TAG,"data0수"+partyCount);
                                if(partyCount == 0)
                                {
                                    /* 참여자가 모두 없어지면 앨범이 없어짐:DB */
                                    myRef.child(mAlbumKey).removeValue();
                                }else{
                                    /* 주인장이 없어지면 주인장을 바꿈*/
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        uid = child.getKey();
                                        if(!mAlbumOwner.equals(uid))
                                        {
                                            myRef.child(mAlbumKey).child("owner").setValue(uid);
                                            break;
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


//                        if(partyCount == 0)
//                        {
//                            /* 참여자가 모두 없어지면 앨범이 없어짐:File */
//                            for(int j=0; j<filenameList.size();++j){
//                                // Create a reference to the file to delete
//                                StorageReference desertRef = storageRef.child(mAlbumKey+"/"+filenameList.get(j));
//
//                                // Delete the file
//                                desertRef.delete().addOnSuccessListener(new OnSuccessListener() {
//                                    @Override
//                                    public void onSuccess(Object object) {
//                                        // File deleted successfully
//                                        Log.i(TAG,"Remove File Success: mAlbumKey:"+mAlbumKey);
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception exception) {
//                                        // Uh-oh, an error occurred!
//                                        Log.i(TAG,"Remove File Fail: mAlbumKey:"+mAlbumKey);
//                                    }
//                                });
//                            }
//                        }

                        Intent intent = new Intent(activity, AlbumsActivity.class);
                        startActivity(intent);
                        /* Group 저장 데이터 및 검색 기준 우선 설정 후 구현 */
//                        groupRef.child(mAlbumKey).child()
                    }
                });
                bld.setNegativeButton("취소",null);
                bld.show();
                return true;

            case R.id.action_invite:
                Intent intent = new Intent(this, AddNewMemberActivity.class);
                intent.putExtra("albumkey",mAlbumKey);
                intent.putExtra("menu","invite");
                startActivity(intent);
                return true;

            case R.id.action_addpicture:
                Intent intent2 = new Intent(this, ImagePickActivity.class);
                intent2.putExtra("startDate", mStartDate);
                startActivityForResult(intent2, REQ_CODE_PICK_PICTURE);
                /*Intent i = new Intent(Intent.ACTION_PICK);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // images on the SD card.

                // 결과를 리턴하는 Activity 호출
                startActivityForResult(i, REQ_CODE_PICK_PICTURE);
*/
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_CODE_PICK_PICTURE){
                ClipData clipData = data.getClipData();
                int total = 0;
                Uri uri = null;
                if(clipData == null) {
                    uri = data.getData();
                    total = 1;
                }
                else {
                    total = clipData.getItemCount();
                }

                for (int i = 0; i < total; i++) {
                    if(clipData != null) {
                        uri = clipData.getItemAt(i).getUri();
                    }
                    final Uri imageUri = uri;
                    final String imagePath = imageUri.getPath();
                    final DatabaseReference mReference = myRef.child(mAlbumKey).child("filelist");
                    final StorageReference albumReference = storageRef.child(mAlbumKey);
                    StorageReference imageRef = albumReference.child(uri.getLastPathSegment());

                    final int count = i + 1;
                    final int totalCount = total;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;

                    Bitmap src = null;
                    try {
                        src = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);
                        //i.setImageBitmap(bt);
                    } catch (Exception e) {
                    }

                    float ratio = (float)src.getHeight()/(float)src.getWidth();
                    //Bitmap src = BitmapFactory.decodeFile(imagePath, options);
                    Bitmap resized = Bitmap.createScaledBitmap(src, 100, (int)(100.0*ratio), true);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resized.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();

                    UploadTask uploadTask = imageRef.putBytes(bytes);
                    //
                    //UploadTask uploadTask = imageRef.putFile(imageUri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            TextView progressView = (TextView) findViewById(R.id.progress); //khlee
                            progressView.setVisibility(View.GONE); //khlee

                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            DatabaseReference r = mReference.push();
                            String filename = taskSnapshot.getMetadata().getName();
                            r.child("url").setValue(downloadUrl.toString());
                            r.child("filename").setValue(filename);
                            r.child("owner").setValue(mUser.getUid());

                            //앨범 썸네일 값이 DEFALUT라면(=앨범에 처음으로 이미지를 추가하는 경우라면) 썸네일을 최초 추가된 이미지로 변경한다.
                            if("DEFALUT".equals(mThumbnail)){   //mThumbnail이 null일 경우에 대한 방어코드.
                                myRef.child(mAlbumKey).child("thumbnail").setValue(downloadUrl.toString());
                            }

                            albumList.add(downloadUrl.toString());
                            gridAdapter.notifyDataSetChanged();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            TextView progressView = (TextView) findViewById(R.id.progress);
                            progressView.setVisibility(View.VISIBLE); //khlee
                            double progress = 100.0 * count * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) / totalCount;
                            System.out.println("Upload is " + progress + "% done");
                            progressView.setText("업로딩: " + progress + "%");
                        }
                    });
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
