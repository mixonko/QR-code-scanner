package com.myapp.test.qrcodescanner;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myapp.test.qrcodescanner.ScannerActivity.EXTRA_QRSTRING;

public class MainActivity extends AppCompatActivity {
    private Button start;
    private Button load;
    private TextView link;
    private String result;
    private AdView mAdView;
    static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ads
        MobileAds.initialize(this, "ca-app-pub-3602084545548553~3024646952");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        start = findViewById(R.id.start);
        load = findViewById(R.id.load);
        load.setVisibility(View.INVISIBLE);
        link = findViewById(R.id.link);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });


        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result != null) {
                    extractAndOpenUrls(result);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                result = data.getStringExtra(EXTRA_QRSTRING);
                link.setText(result);
                load.setVisibility(View.VISIBLE);
            }

        }

    }


    private void startScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void extractAndOpenUrls(String text) {
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern urlPattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = urlPattern.matcher(text);

        if (urlMatcher.find()) {
            String url = text.substring(urlMatcher.start(0), urlMatcher.end(0));
            Intent urlIntent = new Intent(Intent.ACTION_VIEW);
            urlIntent.setData(Uri.parse(url));
            startActivity(urlIntent);
        }

        Scanner s = new Scanner(text);

        String email = s.findInLine(Pattern.compile("MATMSG:TO:(.*);SUB"));
        if (email != null) {
            email = (String) email.subSequence(10, email.length() - 4);

            s.close();

            String mailto = "mailto:" + Uri.encode(email);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));

            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No apps for sending emails",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}
