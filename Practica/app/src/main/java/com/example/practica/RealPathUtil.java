package com.example.practica;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RealPathUtil {

    public static String getRealPath(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getFilePathFromUri(context, uri);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getRealPathFromURI_API19(context, uri);
        } else {
            return getRealPathFromURI_BelowAPI11(context, uri);
        }
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            String displayName = "temp_file";

            Cursor cursor = resolver.query(uri, new String[]{
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.MIME_TYPE
            }, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                cursor.close();
            }

            File tempFile = File.createTempFile("upload_", ".tmp", context.getCacheDir());
            tempFile.deleteOnExit();

            InputStream inputStream = resolver.openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("RealPathUtil", "Ошибка получения пути файла", e);
            return null;
        }
    }

    @SuppressLint("NewApi")
    private static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (documentId.startsWith("raw:")) {
                return documentId.substring(4);
            }

            String[] split = documentId.split(":");
            String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            if (contentUri != null) {
                String selection = "_id=?";

                String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }

        return filePath;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{column}, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(column);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}