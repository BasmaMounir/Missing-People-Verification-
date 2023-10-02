package com.example.myapplicationbeso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.myapplicationbeso.ml.TfLiteModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.tensorflow.lite.support.image.TensorImage;

public class AddImage extends AppCompatActivity {
    Button BSelectImage;
    ImageView IVPreviewImage;
    int SELECT_PICTURE = 200;
    private static final int pic_id = 123;
    Bitmap  imgOfModel=null;
    Bitmap  imgOfModel2=null;
    int image_size=128;
    Button start_model;
    FirebaseStorage storage;

    int minIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        BSelectImage = findViewById(R.id.BSelectImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        start_model = findViewById(R.id.camera_button3);

        int[] imageIds = {R.drawable.image1, R.drawable.image2, R.drawable.david, R.drawable.imags4,R.drawable.basma,R.drawable.alia};
        float[] result =new float[imageIds.length];

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Tutorials");
        storage= FirebaseStorage.getInstance();

        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        Log.i("Imagepath","befor");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Android Tutorials");
        List<DataClass> users = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataClass user = userSnapshot.getValue(DataClass.class);
                    users.add(user);
                }

                for (DataClass user : users) {
                    // Do something with each user, such as logging their name or displaying their image
                    Log.d("User", user.getName());
                    Log.d("User", user.getDataImage());
                    /*Glide.with(AddImage.this)
                            .load(user.getDataImage())
                            .into(imageView);*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while retrieving the data
            }
        });

        start_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_LONG).show();

                for (int i = 0; i < imageIds.length; i++) {
                    Bitmap img = BitmapFactory.decodeResource(AddImage.this.getResources(), imageIds[i]);
                    result[i] = distance(use_model(imgOfModel), use_model(img));
                    //Toast.makeText(getApplicationContext(), "imageIds[i]  "+bitmap , Toast.LENGTH_LONG).show();
                }
                float minValue = result[0]; // start with first element as minimum
                for (int i = 1; i < result.length; i++) {
                    if (result[i] < minValue) {
                        minValue = result[i];
                        minIndex = i;
                    }
                }

                if (minValue <0.09)
                {
                    Intent intent = new Intent(AddImage.this, Sorry.class);
                    startActivity(intent);

                } else {
                    String imageName = getResources().getResourceEntryName(imageIds[minIndex]);
                    //Toast.makeText(getApplicationContext(), "Image name  "+imageName, Toast.LENGTH_LONG).show();

                    String fileContents = "";
                    String[] filename = {"file1.txt", "file2.txt", "file3.txt", "file4.txt", "file5.txt", "file6.txt"};
                    try {
                        fileContents = readTextFromAssets(getApplicationContext(), filename[minIndex]);
                        FileOutputStream outputStream = openFileOutput(filename[minIndex], Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Error reading file from assets", Toast.LENGTH_SHORT).show();
                    }

                    String[] lines = fileContents.split("\n");

                    Intent intent = new Intent(AddImage.this, DetailActivity.class);
                    String imageUrl = getImageUrl(imageName);
                    //Toast.makeText(getApplicationContext(), "imageUrl  "+imageUrl, Toast.LENGTH_LONG).show();
                    intent.putExtra("Image", imageUrl);

                    for (String line : lines) {
                        String[] parts = line.split(": ");
                        if (parts.length == 2) {
                            String label = parts[0];
                            String value = parts[1];
                            switch (label) {
                                case "Name":
                                    intent.putExtra("Title", value);
                                    break;
                                case "Age":
                                    intent.putExtra("Age", value);
                                    break;
                                case "Phone":
                                    intent.putExtra("Phone", value);
                                    break;
                                case "Location":
                                    intent.putExtra("Location", value);
                                    break;
                                case "Date":
                                    intent.putExtra("Date", value);
                                    break;
                                case "ID":
                                    intent.putExtra("ID", value);
                                    break;
                                case "Email":
                                    intent.putExtra("Email", value);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    startActivity(intent);

                }
            }
        });

    }
    // this function is triggered when
    // the Select Image Button is clicked

    public String getImageUrl(String imageName) {
        int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
        return uri.toString();
    }
    public String readTextFromAssets(Context context, String filename) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    void imageChooser() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id)
        { //camera option
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgOfModel = ThumbnailUtils.extractThumbnail(imgOfModel, image_size, image_size);
            imgOfModel = Bitmap.createScaledBitmap(imgOfModel, image_size, image_size, false);
            // Set the image in imageview for display
            IVPreviewImage.setImageBitmap(imgOfModel);
        }
        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                try
                {
                    imgOfModel=MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);
                }
                catch (IOException e)
                {
                    //throw new RuntimeException(e);
                    e.printStackTrace();
                }

                //imgOfModel = Bitmap.createScaledBitmap(imgOfModel,image_size,image_size,false);

                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // IVPreviewImage.setImageURI(selectedImageUri);
                    IVPreviewImage.setImageBitmap(imgOfModel);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //Here the code of model

    public String convertFirebaseStorageUrlToGcsUrl(String firebaseStorageUrl) {
        String bucket = firebaseStorageUrl.substring(firebaseStorageUrl.indexOf("/v0/b/") + 6, firebaseStorageUrl.indexOf("/o/"));
        String pathWithEncodedCharacters = firebaseStorageUrl.substring(firebaseStorageUrl.indexOf("/o/") + 3, firebaseStorageUrl.indexOf("?alt=media"));
        String path;
        try {
            path = java.net.URLDecoder.decode(pathWithEncodedCharacters, "UTF-8").replace("%2F", "/");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        String gcsUrl = "gs://" + bucket + "/" + path;

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(gcsUrl);
        String fileName = storageRef.getName();

        return fileName;
    }
    public Bitmap getBitmapFromFirebaseUrl(String firebaseUrl) {
        try {
            URL urlObject = new URL(firebaseUrl);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public float distance(float[] a, float[] b)
    {
        float diff =0;
        int N ;
        if(a.length < b.length)
            N=a.length;
        else
            N=b.length;

        float sum = 0;
        for (int i = 0; i <N; i++) {
            diff += a[i] - b[i];
            // Toast.makeText(getApplicationContext(), "diff  "+diff, Toast.LENGTH_LONG).show();

        }

        float euclideanDistance = (float) Math.sqrt(Math.abs(diff));

        return euclideanDistance;
    }

    private  static ImageProcessor imageProcessor=new ImageProcessor.Builder()
            .add(new ResizeOp(128,128,ResizeOp.ResizeMethod.BILINEAR))
            .build();
    public float[] use_model(Bitmap img1)
    {
        try {
            TfLiteModel model = TfLiteModel.newInstance(AddImage.this);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
            TensorImage tensorImage=new TensorImage(DataType.FLOAT32);
            tensorImage.load(img1);
            tensorImage=imageProcessor.process(tensorImage);
            ByteBuffer byteBuffer=tensorImage.getBuffer();
            //imgOfModel=Bitmap.createScaledBitmap(imgOfModel,image_size,image_size,true);
            // inputFeature0.loadBuffer(TensorImage.fromBitmap(imgOfModel).getBuffer());
            inputFeature0.loadBuffer(byteBuffer);

            TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
            //inputFeature1.loadBuffer(byteBuffer);

            TensorBuffer inputFeature2 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
            //inputFeature2.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            TfLiteModel.Outputs outputs = model.process(inputFeature0, inputFeature1, inputFeature2);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            TensorBuffer outputFeature1 = outputs.getOutputFeature1AsTensorBuffer();


            // Releases model resources if no longer used.
            model.close();
            return outputFeature1.getFloatArray();
        }
        catch (IOException e)
        {
            // TODO Handle the exception
        }
        return  null;
    }
    public  void apply_ModelAll_images()
    {
        float[] res1= use_model(imgOfModel);
        int img_number=5;
        float[] all_distance =new float[img_number];
        for(int i=0;i<img_number;i++){
            Bitmap currentImg=null;//allimg[i]
            float[] res2= use_model(currentImg);
            all_distance[i]=(distance(res1,res2));

        }
        //all_distance

    }

    public  Bitmap fun(){

        List<Bitmap> images=new List<Bitmap>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<Bitmap> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] ts) {
                return null;
            }

            @Override
            public boolean add(Bitmap bitmap) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends Bitmap> collection) {
                return false;
            }

            @Override
            public boolean addAll(int i, @NonNull Collection<? extends Bitmap> collection) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public Bitmap get(int i) {
                return null;
            }

            @Override
            public Bitmap set(int i, Bitmap bitmap) {
                return null;
            }

            @Override
            public void add(int i, Bitmap bitmap) {

            }

            @Override
            public Bitmap remove(int i) {
                return null;
            }

            @Override
            public int indexOf(@Nullable Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(@Nullable Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<Bitmap> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<Bitmap> listIterator(int i) {
                return null;
            }

            @NonNull
            @Override
            public List<Bitmap> subList(int i, int i1) {
                return null;
            }
        };
        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Android Images/1000068255.png");
        Log.i("second log","after storage");
        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> srList=listResult.getItems();
                        for(StorageReference sr:srList)
                        {
                            Log.i("3Imagepath","sr.getPath()");
                            Log.i("Imagepath2",sr.getPath());
                            long SIZE=1024*1024;
                            sr.getBytes(Long.MAX_VALUE)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            //if(bytes !=null && bytes.length>0){

                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                            imgOfModel2=bitmap;
                                            //images.add(bitmap);
                                            IVPreviewImage.setImageBitmap(bitmap);
                                            //}
                                        }
                                    });
                        }

                    }
                });
        return imgOfModel2;
    }

}
