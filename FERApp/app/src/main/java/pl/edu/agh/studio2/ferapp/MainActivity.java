package pl.edu.agh.studio2.ferapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//import java.util.Base64; //od API Level 26 (Android 8), a mamy 21 (Android 5)
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;

//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    static final String url = "https://ferappagh.azurewebsites.net/api/FER";
//    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    TextView txtRandomEmotion;
    Emotion randomEmotion;
    Button btnTakePhoto;
    Button btnSendRes;
    Button btnTryAgain;
    ImageView imgEmoji;

    Uri fileUri;
    Uri photoURI;
    String currentPhotoPath;
    ImageView imgPhoto;
//    byte[] photo_byte_array;
    String encodedPhoto;
    String imageFileName;

//    private OkHttpClient client;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        client = new OkHttpClient();
        requestQueue = Volley.newRequestQueue(this);

        txtRandomEmotion = (TextView) findViewById(R.id.txt_random_emo);
        imgEmoji = findViewById(R.id.img_emoji);
        setRandomEmotion();

        imgPhoto = findViewById(R.id.img_photo_taken);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSendRes = findViewById(R.id.btn_send_res);
        btnTryAgain = findViewById(R.id.btn_try_again);



        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent2();
            }
        });

        btnSendRes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(),"sending photo...", Toast.LENGTH_SHORT).show();
                    sendPhoto();
                    Toast.makeText(getApplicationContext(),"...photo sent", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"sendPhotoException", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setRandomEmotion();
                imgPhoto.setImageURI(null);
                fileUri = null;
                photoURI = null ;
                currentPhotoPath = "";
                encodedPhoto = "";
            }
        });


    }

    private void sendPhoto() throws JSONException {

        if(imageFileName == null || imageFileName.isEmpty() || encodedPhoto == null || encodedPhoto.isEmpty()) {
            Toast.makeText(getApplicationContext(),"Empty json!", Toast.LENGTH_LONG).show();
            return;
        }

        new PostRequestAsyncTask(this).execute(url,imageFileName, encodedPhoto, randomEmotion.name());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri();
        takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, fileUri );

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Toast.makeText(getApplicationContext(),"ActivityNotFoundException", Toast.LENGTH_SHORT).show();
        }
    }
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "FERApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("FERApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getApplicationContext(),"Error occurred while creating the File", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "pl.edu.agh.studio2.ferapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }



    //@SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            img_photo.setImageBitmap(imageBitmap);
//            encodePhoto(imageBitmap);
//            Uri imgUri = data.getData(); //The default Android camera application returns a non-null intent only when passing back a thumbnail in the returned Intent. If you pass EXTRA_OUTPUT with a URI to write to, it will return a null intent and the picture is in the URI that you passed in.
            imgPhoto.setImageURI(null);
            imgPhoto.setImageURI(photoURI);

            try {
                encodePhoto(photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void encodePhoto(Bitmap imageBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] photo_byte_array = stream.toByteArray();

//        String encoded = Base64.getEncoder().encodeToString("Hello".getBytes());
        Base64 codec = new Base64();
        encodedPhoto = codec.encodeBase64String (photo_byte_array);
//        Toast.makeText(getApplicationContext(),encodedPhoto, Toast.LENGTH_LONG).show();
    }

    private void encodePhoto(Uri photoURI) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(photoURI);
        byte[] photoByteArray = getBytes(iStream);
        Base64 codec = new Base64();
        encodedPhoto = codec.encodeBase64String(photoByteArray);
//        Toast.makeText(getApplicationContext(),encodedPhoto, Toast.LENGTH_LONG).show();
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    void setRandomEmotion(){
        randomEmotion = Emotion.randomEmotion();
        txtRandomEmotion.setText(randomEmotion.name());  //String.valueOf(random_emotion))
        imgEmoji.setImageDrawable(getResources().getDrawable(randomEmotion.getDrawable()));
    }

}
