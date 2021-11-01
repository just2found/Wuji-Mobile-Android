package libs.source.common.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2018/04/20
 *     desc  : URI 相关
 * </pre>
 */
public final class UriUtils {

    private static final String TAG = "UriUtils";

    private UriUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Uri to file.
     *
     * @param uri The uri.
     * @return file
     */
    public static File uri2File(Context context,@NonNull final Uri uri) {
        Timber.d(uri.toString());
        String authority = uri.getAuthority();
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            String path = uri.getPath();
            if (path != null) return new File(path);
            Timber.d(uri.toString() + " parse failed. -> 0");
            return null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return new File(Environment.getExternalStorageDirectory() + "/" + split[1]);
                }
                Timber.d(uri.toString() + " parse failed. -> 1");
                return null;
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id)
                );
                return getFileFromUri(context,contentUri, 2);
            } else if ("com.android.providers.media.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    Timber.d(uri.toString() + " parse failed. -> 3");
                    return null;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getFileFromUri(context,contentUri, selection, selectionArgs, 4);
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                return getFileFromUri(context,uri, 5);
            } else {
                Timber.d("%s parse failed. -> 6", uri.toString());
                return null;
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return getFileFromUri(context,uri, 7);
        } else {
            Timber.d("%s parse failed. -> 8", uri.toString());
            return null;
        }
    }

    private static File getFileFromUri(Context context,final Uri uri, final int code) {
        return getFileFromUri(context,uri, null, null, code);
    }

    private static File getFileFromUri(Context context,final Uri uri,
                                       final String selection,
                                       final String[] selectionArgs,
                                       final int code) {
        final Cursor cursor = context.getContentResolver().query(
                uri, null, selection, selectionArgs, null);
        if (cursor == null) {
            Timber.d(uri.toString() + " parse failed(cursor is null). -> " + code);
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex("_data");
                if (columnIndex > -1) {
                    return new File(cursor.getString(columnIndex));
                } else {
                    Timber.d(uri.toString() + " parse failed(columnIndex: " + columnIndex + " is wrong). -> " + code);
                    return null;
                }
            } else {
                Timber.d(uri.toString() + " parse failed(moveToFirst return false). -> " + code);
                return null;
            }
        } catch (Exception e) {
            Timber.d(uri.toString() + " parse failed. -> " + code);
            return null;
        } finally {
            cursor.close();
        }
    }


    public static final String URI_CONTENT_SCHEME = ContentResolver.SCHEME_CONTENT;


    public static @Nullable
    String getDisplayNameForUri(Uri uri, Context context) {

        if (uri == null || context == null) {
            throw new IllegalArgumentException("Received NULL!");
        }

        String displayName;

        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            displayName = uri.getLastPathSegment();     // ready to return

        } else {
            // content: URI

            displayName = getDisplayNameFromContentResolver(uri, context);

            try {
                if (displayName == null) {
                    // last chance to have a name
                    displayName = uri.getLastPathSegment().replaceAll("\\s", "");
                }

                // Add best possible extension
                int index = displayName.lastIndexOf('.');
                if (index == -1 || MimeTypeMap.getSingleton().
                        getMimeTypeFromExtension(displayName.substring(index + 1).toLowerCase(Locale.ROOT)) == null) {
                    String mimeType = context.getContentResolver().getType(uri);
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        displayName += "." + extension;
                    }
                }

            } catch (Exception e) {
                Timber.d("No way to get a display name for " + uri.toString());
            }
        }

        // Replace path separator characters to avoid inconsistent paths
        return displayName != null ? displayName.replaceAll("/", "-") : null;
    }


    private static String getDisplayNameFromContentResolver(Uri uri, Context context) {
        String displayName = null;
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType != null) {
            String displayNameColumn;
            if (MIMETypeUtils.isImage(mimeType)) {
                displayNameColumn = MediaStore.Images.ImageColumns.DISPLAY_NAME;

            } else if (MIMETypeUtils.isVideo(mimeType)) {
                displayNameColumn = MediaStore.Video.VideoColumns.DISPLAY_NAME;

            } else if (MIMETypeUtils.isAudio(mimeType)) {
                displayNameColumn = MediaStore.Audio.AudioColumns.DISPLAY_NAME;
            } else {
                displayNameColumn = MediaStore.Files.FileColumns.DISPLAY_NAME;
            }

            try (Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{displayNameColumn},
                    null,
                    null,
                    null
            )) {
                if (cursor != null) {
                    cursor.moveToFirst();
                    displayName = cursor.getString(cursor.getColumnIndex(displayNameColumn));
                }

            } catch (Exception e) {
                Timber.d("Could not retrieve display name for %s", uri.toString());
                // nothing else, displayName keeps null

            }
        }
        return displayName;
    }

}
