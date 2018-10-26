package com.ctel.videodownloader.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URLL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "service receiver";

    public DownloadService() {
        super("DownloadService");
    }

    // Will be called asynchronously by OS.
    @Override
    protected void onHandleIntent(Intent intent) {
        String urlPath = intent.getStringExtra(URLL);
        String fileName = intent.getStringExtra(FILENAME);
        File output = new File(fileName);
        if (!output.exists()) {
            output.mkdir();
        }

        InputStream stream = null;
        FileOutputStream fos = null;
        try {
            int count;
            URL url = new URL(urlPath);
            stream = url.openConnection().getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);
        //    fos = new FileOutputStream(output.getPath());
       //     int next = -1;

        /*    while ((next = reader.read()) != -1) {
                fos.write(next);
            }*/

            // Successful finished
            result = Activity.RESULT_OK;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        publishResults(output.getAbsolutePath(), result, urlPath);
    }

    private void publishResults(String outputPath, int result, String url) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(URLL, url);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }


}