package com.client.talkster.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.client.talkster.MyApplication;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Response;

public class FileUtils implements IActivity, IAPIResponseHandler {

    //extends AppCompatActivity
    private UserJWT userJWT;
    private boolean imageReceived;
    private Bitmap image;

    public FileUtils() {}

    public FileUtils(UserJWT userJWT) {this.userJWT = userJWT;}

    public Bitmap getProfilePicture(long userID){
        APIHandler<Object, FileUtils> apiHandler = new APIHandler<>(this);
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_FILE_GET_PROFILE+"/"+userID, userJWT.getAccessToken());
        imageReceived = false;
        while(!imageReceived) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    public static byte[] getBytes(Uri uri, ContentResolver cr) throws IOException {
        InputStream inputStream = cr.openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static MediaType getType(Uri uri, ContentResolver cr){
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                .toString());
        MediaType mediaType = MediaType.get(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase()));
        return mediaType;
    }

    public static String getFilename(Uri uri, ContentResolver cr){
        return uri.getLastPathSegment();
    }

    public static Bitmap getMarker(Bitmap bitmap){
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inScaled = false;
        Bitmap marker = BitmapFactory.decodeResource(MyApplication.getAppContext().getResources(),
                R.drawable.marker, bfo);
        bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true);
        bitmap = circleCrop(bitmap);
        Bitmap bmOverlay = Bitmap.createBitmap(marker.getWidth(), marker.getHeight(), marker.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawBitmap(marker, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap circleCrop(Bitmap bitmap){
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // Initialize a new instance of Bitmap
        Bitmap circleBitmap = Bitmap.createBitmap (
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        Canvas canvas = new Canvas(circleBitmap);
        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth-bitmap.getWidth())/2;
        float top = (squareBitmapWidth-bitmap.getHeight())/2;
        canvas.drawBitmap(bitmap, left, top, paint);
        // Free the native object associated with this bitmap.
        bitmap.recycle();
        // Return the circular bitmap
        return circleBitmap;
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl) {
        try {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);
            int responseCode = response.code();

            // profilePicture response
            if(apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_GET_PROFILE))
            {
                if (responseCode != 200){
                    if (responseCode == 404 || responseCode == 500){
                        BitmapFactory.Options bfo = new BitmapFactory.Options();
                        bfo.inScaled = false;
                        image = BitmapFactory.decodeResource(MyApplication.getAppContext().getResources(),
                                R.drawable.blank_profile, bfo);
                        imageReceived = true;
                    }
                    else {
                        throw new UserUnauthorizedException("Unexpected response " + response);
                    }
                } else {
                    image = BitmapFactory.decodeStream(response.body().byteStream());
                    imageReceived = true;
                }
            }
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) {

    }

    @Override
    public void getUIElements() {}

    @Override
    public void getBundleElements() {}
}
