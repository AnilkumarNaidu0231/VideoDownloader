package com.ctel.videodownloader.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctel.videodownloader.R;
import com.ctel.videodownloader.service.DownloadService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.app.Activity.RESULT_OK;

/**
 * Created by chandu on 10/24/2018.
 */

public class ProgressFragment extends Fragment {

    WebView webView;
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";

    ProgressBar progressBar;
    String url;
    TextView txtPercentage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        progressBar = view.findViewById(R.id.progressBar);
txtPercentage=view.findViewById(R.id.txtPercentage);
        progressBar.setMax(100);
        if (HomeFragment.mFilePath != null) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            intent.putExtra(DownloadService.FILENAME, HomeFragment.mFilePath);
            intent.putExtra(DownloadService.URLL,
                    HomeFragment.videoData);

            getActivity().startService(intent);

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(
                DownloadService.NOTIFICATION));
        Log.d("TAG", "onResume: " + url);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString(DownloadService.FILEPATH);
                url = bundle.getString(DownloadService.URLL);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (url != null)
                    new DownloadFile().execute(url);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getActivity(),
                            "Download complete. Download URI: " + string,
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getActivity(), "Download failed",
                            Toast.LENGTH_LONG).show();

                }
            }
        }
    };


    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
            this.progressDialog = new ProgressDialog(getActivity());
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            // this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String mBaseFolderPath = android.os.Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "FacebookVideos" + File.separator;
                String mFilePath = "file://" + mBaseFolderPath + "/" + HomeFragment.videoId + ".mp4";
              /*  String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());

                //Append timestamp to file name
                fileName = timestamp + "_" + fileName;

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/";*/

                //Create androiddeft folder if it does not exist
                File directory = new File(mBaseFolderPath, mFilePath);

                if (!new File(mBaseFolderPath).exists()) {
                    new File(mBaseFolderPath).mkdir();
                }
                // Output stream to write file
                // OutputStream output = new FileOutputStream(directory);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));


                    // writing data to file
                    // output.write(data, 0, count);
                }

                // flushing output
                //   output.flush();

                // closing streams
                //   output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
            progressBar.setProgress(Integer.parseInt(progress[0]));
            //  progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading

        }
    }
}
