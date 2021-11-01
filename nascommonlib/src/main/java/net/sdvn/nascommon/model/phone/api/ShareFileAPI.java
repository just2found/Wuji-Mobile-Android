package net.sdvn.nascommon.model.phone.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.MIMETypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class ShareFileAPI {


    public boolean share(@NonNull List<LocalFile> files, @NonNull Context context) {
        boolean multiple = files.size() > 1;
        ArrayList<Uri> uris = new ArrayList<>();
        Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND);
        if (!multiple) {
            String mimeType = MIMETypeUtils.getMIMEType(files.get(0).getName());
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, FileUtils.getFileProviderUri(files.get(0).getFile()));
        } else {
            for (int i = 0; i < files.size(); i++) {
                Uri uri = FileUtils.getFileProviderUri(files.get(i).getFile());
                uris.add(uri);
            }
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(intent);

        return true;
    }
}
