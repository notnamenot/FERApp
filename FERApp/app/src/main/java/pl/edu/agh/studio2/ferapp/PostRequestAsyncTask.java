package pl.edu.agh.studio2.ferapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import static com.android.volley.Response.success;

public class PostRequestAsyncTask extends AsyncTask<String, Integer, Long> {

    RequestQueue requestQueue;

    private Context contextRef;

    public PostRequestAsyncTask(Context context) {
        contextRef = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    protected Long doInBackground(String... strings) {

        String url = strings[0];
        String imageFileName = strings[1];
        String encodedPhoto = strings[2];
        String random_emotion = strings[3];

        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put("id", imageFileName)
                    .put("image", encodedPhoto)
                    .put("emotionID", Emotion.valueOf(random_emotion).getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        String jsonString = json.toString();

//        Toast.makeText(getApplicationContext(),jsonString, Toast.LENGTH_LONG).show();

        /*RequestBody body = RequestBody.create(JSON, jsonString);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();*/

        try {

//            final String requestBody = jsonString;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("VOLLEY", response.toString());
                    System.out.println(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    error.printStackTrace();
                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }


                @Override
                protected Response parseNetworkResponse(NetworkResponse response) {
                    try {
                        String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject("{'status':"+String.valueOf(response.statusCode)+"}"),HttpHeaderParser.parseCacheHeaders(response));
//                        return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException e) {
                        return Response.error(new ParseError(e));
                    } catch (JSONException je) {
                        return Response.error(new ParseError(je));
                    }
                }
            };

//            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.i("VOLLEY", response);
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.e("VOLLEY", error.toString());
//                }
//            }) {
//                @Override
//                public String getBodyContentType() {
//                    return "application/json; charset=utf-8";
//                }
//
//                @Override
//                public byte[] getBody() throws AuthFailureError {
//                    try {
//                        return requestBody == null ? null : requestBody.getBytes("utf-8");
//                    } catch (UnsupportedEncodingException uee) {
//                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
//                        return null;
//                    }
//                }
//
//                @Override
//                protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                    String responseString = "";
//                    if (response != null) {
//                        responseString = String.valueOf(response.statusCode);
//                        // can get more details such as response.headers
//                    }
//                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//                }
//            };

//            requestQueue.add(stringRequest);
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        Toast.makeText(contextRef,progress.toString(), Toast.LENGTH_LONG).show();
    }
}
