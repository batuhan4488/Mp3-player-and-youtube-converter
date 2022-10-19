package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class PlayerDowload extends AppCompatActivity {

     String youtubeLink;
    String newlink;
    EditText editText;
    TextView musicname;
    LinearLayout mainLayout;
    ProgressBar mainProgressBar;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_dowload);
        editText = (EditText)findViewById(R.id.link);
        button = findViewById(R.id.buton);
        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar = findViewById(R.id.prgrBar);
        musicname =findViewById(R.id.musicname);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ytLink = editText.getText().toString();
                if (ytLink != null
                        && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
                    youtubeLink = ytLink;
                    // We have a valid link
                    getYoutubeDownloadUrl(youtubeLink);
                } else {
                    Toast.makeText(getApplicationContext(),"Bilgilendirme mesajı",Toast.LENGTH_LONG).show();
                    finish();
                }





            }
        });


    }

    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(this) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mainProgressBar.setVisibility(View.GONE);

                if (ytFiles == null) {
                    // Something went wrong we got no urls. Always check this.
                    finish();
                    Toast.makeText(getApplicationContext(),"Bir sorun oluştu Lütfen Tekrar Deneyiniz!",Toast.LENGTH_LONG).show();
                    return;

                }
                // Iterate over itags
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    // ytFile represents one file with its url and meta data
                    YtFile ytFile = ytFiles.get(itag);


                    // Just add videos in a decent format => height -1 = audio
                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addButtonToMainLayout(vMeta.getTitle(), ytFile);

                    }
                }
            }
        }.extract(youtubeLink);
    }

    private void addButtonToMainLayout(final String videoTitle, final YtFile ytfile) {
        // Display some buttons and let the user choose the format
        String btnText = (ytfile.getFormat().getHeight() == -1) ? "Audio " +
                ytfile.getFormat().getAudioBitrate() + " kbit/s" :
                ytfile.getFormat().getHeight() + "p";
        btnText += (ytfile.getFormat().isDashContainer()) ? " dash" : "";
        musicname.setText(videoTitle);
        Button btn = new Button(this);
        btn.setText(btnText);
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        btn.setOnClickListener(v -> {
            String filename;
            if (videoTitle.length() > 55) {
                filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
            } else {
                filename = videoTitle + "." + ytfile.getFormat().getExt();
            }
            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
            downloadFromUrl(ytfile.getUrl(), videoTitle, filename);
            Toast.makeText(getApplicationContext(),"İndirme Başladı Lütfen Bekleyiniz",Toast.LENGTH_LONG).show();
            finish();




        });
        mainLayout.addView(btn);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    }



