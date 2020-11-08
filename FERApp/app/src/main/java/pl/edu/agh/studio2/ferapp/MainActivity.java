package pl.edu.agh.studio2.ferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//import java.util.Base64; //od API Level 26 (Android 8), a mamy 21 (Android 5)
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String url = "https://ferappagh.azurewebsites.net/api/FER";
//    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    TextView txt_random_emotion;
    Emotion random_emotion;
    Button btn_take_photo;
    Button btn_send_res;
    ImageView img_photo;
//    byte[] photo_byte_array;
    String encodedPhoto;

//    private OkHttpClient client;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        client = new OkHttpClient();
        requestQueue = Volley.newRequestQueue(this);

        random_emotion = Emotion.randomEmotion();

        txt_random_emotion = (TextView) findViewById(R.id.txt_random_emo);
        txt_random_emotion.setText(random_emotion.name());  //String.valueOf(random_emotion))

        btn_take_photo = findViewById(R.id.btn_take_photo);
        img_photo = findViewById(R.id.img_photo_taken);

        btn_send_res = findViewById(R.id.btn_send_res);

        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btn_send_res.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    sendPhoto();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"sendPhotoException", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void sendPhoto() throws JSONException {
        JSONObject json = new JSONObject()
                .put("id","randomid_jdfhg")
                .put("image", encodedPhoto)
                .put("emotionID", random_emotion.getId());
        String jsonString = json.toString();

        Toast.makeText(getApplicationContext(),jsonString, Toast.LENGTH_LONG).show();

        /*RequestBody body = RequestBody.create(JSON, jsonString);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();*/

        try {

            final String requestBody = jsonString;

    //            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
    //                @Override
    //                public void onResponse(JSONObject response) {
    //                    System.out.println(response);
    //                }
    //            }, new Response.ErrorListener() {
    //                @Override
    //                public void onErrorResponse(VolleyError error) {
    //                    error.printStackTrace();
    //                }
    //            });

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Toast.makeText(getApplicationContext(),"ActivityNotFoundException", Toast.LENGTH_SHORT).show();
        }
    }
    //@SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img_photo.setImageBitmap(imageBitmap);
            encodePhoto(imageBitmap);
        }
    }

    private void encodePhoto(Bitmap imageBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] photo_byte_array = stream.toByteArray();

//        String encoded = Base64.getEncoder().encodeToString("Hello".getBytes());
        Base64 codec = new Base64();
        encodedPhoto = codec.encodeBase64String(photo_byte_array);
//        Toast.makeText(getApplicationContext(),encodedPhoto, Toast.LENGTH_LONG).show();
    }


}
