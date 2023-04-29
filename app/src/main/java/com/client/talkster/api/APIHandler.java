package com.client.talkster.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.client.talkster.classes.FileContent;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIHandler<DTO, Activity>
{
    Request request;
    private final Activity activity;
    private final OkHttpClient okHttpClient;
    public final String TALKSTER_SERVER_URL = APIConfig.TALKSTER_SERVER_INTERNET_PROTOCOL + APIConfig.TALKSTER_SERVER_IP + ":" + APIConfig.TALKSTER_SERVER_PORT;

    public APIHandler(Activity activity)
    {
        this.activity = activity;
        okHttpClient = new OkHttpClient();
    }

    public void apiGET(String apiUrl, String jwtToken)
    {
        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .build();

        Log.d("APIHandler", "apiGET: " + request.toString());

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiDELETE(String apiUrl, String jwtToken)
    {
        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .delete()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiPOST(String apiUrl, DTO object, String jwtToken)
    {
        RequestBody body = RequestBody.create(APIConfig.JSON, new Gson().toJson(object));

        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .post(body)
                .build();

        Log.d("APIHandler", "apiPOST: " + request.toString());

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiDELETE(String apiUrl, DTO object, String jwtToken)
    {
        RequestBody body = RequestBody.create(APIConfig.JSON, new Gson().toJson(object));

        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .delete(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiMultipartPOST(String apiUrl, FileContent fileContent, String jwtToken)
    {
        RequestBody body = RequestBody.create(fileContent.getType(), fileContent.getContent());

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileContent.getFilename(), body)// file param
                .build();

        Request request = new Request.Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .post(multipartBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                Log.d("APIHandler", "onFailure: " + e.getMessage());
                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                Log.d("APIHandler", apiUrl + " onResponse: " + response);
                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiPUT(String apiUrl, DTO object, String jwtToken)
    {
        RequestBody body = RequestBody.create(APIConfig.JSON, new Gson().toJson(object));

        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .put(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }

    public void apiMultipartPUT(String apiUrl, FileContent fileContent, String jwtToken)
    {
        RequestBody body = RequestBody.create(fileContent.getType(), fileContent.getContent());

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileContent.getFilename(), body)// file param
                .build();

        Request request = new Request.Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .put(multipartBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            IAPIResponseHandler responseHandler;

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                Log.d("APIHandler", "onFailure: " + e.getMessage());
                responseHandler.onFailure(call, e, apiUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                if(activity instanceof IAPIResponseHandler)
                    responseHandler = (IAPIResponseHandler) activity;

                Log.d("APIHandler", apiUrl + " onResponse: " + response);
                responseHandler.onResponse(call, response, apiUrl);
            }
        });
    }
}
