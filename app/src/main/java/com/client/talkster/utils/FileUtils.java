package com.client.talkster.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;

public class FileUtils {

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

}
