package com.kaedea.mediastore.dualappcompat;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

/**
 * @author Kaede
 * @since 2021/4/26
 */
public final class MediaStoreOps {
    private static final String TAG = "ScopedStorageUtil";

    public static Uri saveWithMediaStore(@NonNull final Context context, @NonNull final String srcFilePath, @NonNull final String destFilePath) {
        if (!new File(srcFilePath).exists()) {
            Log.w(TAG, "#saveWithMediaStore src file not found, path = " + srcFilePath);
            return null;
        }

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String mediaDir = Environment.DIRECTORY_PICTURES;

        if (TextUtils.isEmpty(mediaDir)) {
            Log.w(TAG, "#saveWithMediaStore unsupported contentUri: " + contentUri);
            return null;
        }

        String mimeType = "image/jpg";
        String displayName = srcFilePath.substring(srcFilePath.lastIndexOf("/") + 1);
        String relativePath = null;

        if (destFilePath.contains(mediaDir)) {
            int idxBgn = destFilePath.indexOf(mediaDir) + mediaDir.length();
            int idxEnd = destFilePath.lastIndexOf(File.separator);
            if (idxBgn < idxEnd) {
                relativePath = mediaDir + destFilePath.substring(idxBgn, idxEnd);
            }
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        if (!TextUtils.isEmpty(relativePath)) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = null;

        try {
            uri = resolver.insert(contentUri, contentValues);
            if (uri == null) {
                Log.w(TAG, "#saveWithMediaStore create target media uri fail, path = " + destFilePath);
                return null;
            }
            try (OutputStream os = resolver.openOutputStream(uri)) {
                try (InputStream is = new FileInputStream(srcFilePath)) {
                    IOUtils.copy(is, os);
                    return uri;
                }
            }
        } catch (Throwable e) {
            if (uri != null) {
                try {
                    resolver.delete(uri, null, null);
                } catch (Throwable ignored) {
                }
            }
            Log.e(TAG, "MediaStore save fail", e);
            return null;
        }
    }

    public static String uriToPath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;

        try {
            if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("image".equals(type)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    selection = "_id=?";
                    selectionArgs = new String[]{
                            split[1]
                    };
                }
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                String[] projection = {
                        MediaStore.Images.Media.DATA
                };
                try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
                    if (cursor == null) {
                        return null;
                    }
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Throwable thr) {
            Log.e(TAG, "convert uri 2 path fail", thr);
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static Uri pathToUri(@NonNull Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=? ", new String[]{filePath},
                null
        )) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    return Uri.withAppendedPath(contentUri, String.valueOf(id));
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "convert pathToUri fail", e);
        }

        return null;
    }
}
