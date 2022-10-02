package com.robiultech.languagetranslate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private Intent targetActivity;
    // per app run-- do not show more than 3 fullscreen ad. [[Change it if your want]]
    int fullScreenAdMaxShowCount = 4;
    private TextView translateTv;
    private LinearLayout linearItem,linearItemRateUs;
    private TextInputEditText sourceEdittext;
    private MaterialButton translateButton;
    private Spinner fromSpinner,toSpinner;
    private TextToSpeech textToSpeech;
    private ImageView micTv,idCopy,idShare,idSpeaker;
    RelativeLayout rLayRateUs;

    String [] fromLanguage={"From","English","French","Japanese","Russian","Ukrainian","Chinese","German","Welsh","Sweden","Arabic","Bulgarian","Bengali","Catalan","Czech","Hindi","Urdu"};
    String [] toLanguage={"From","English","French","Japanese","Russian","Ukrainian","Chinese","German","Welsh","Sweden","Arabic","Bulgarian","Bengali","Catalan","Czech","Hindi","Urdu"};
    private static final int REQUEST_PERMISSION_CODE=1;
    int languageCode,fromLanguageCode,toLanguageCode=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdView = findViewById(R.id.adView);
        // linearItem=findViewById(R.id.linearItem);
        translateTv=findViewById(R.id.idTvTranslateTv);
        sourceEdittext=findViewById(R.id.idEditSource);
        translateButton=findViewById(R.id.idButtonTranslate);
        fromSpinner=findViewById(R.id.idFromSpinner);
        toSpinner=findViewById(R.id.idToSpinner);
        micTv=findViewById(R.id.idTvMic);
        idCopy=findViewById(R.id.idCopy);
        idShare=findViewById(R.id.idShare);
        idSpeaker=findViewById(R.id.idSpeaker);
        rLayRateUs = findViewById(R.id.rLayRateUs);
        mAdView.setVisibility(View.GONE);
        linearItemRateUs=findViewById(R.id.idRateUsLayItem);

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                /*
                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK);
                }
                 */
            }
        });
        idSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(""+translateTv.getText().toString(),TextToSpeech.QUEUE_FLUSH,null,null);
            }
        });
        // for ad view
        if (getString(R.string.show_admob_ad).contains("ON")){
            initAdmobAd();
            loadBannerAd();
            loadFullscreenAd();
        }


        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fromLanguageCode=getLanguageCode(fromLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter fromAdapter=new ArrayAdapter(this,R.layout.spinner_item,fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
       toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
               toLanguageCode=getLanguageCode(toLanguage[position]);
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });
        ArrayAdapter toAdapter=new ArrayAdapter(this,R.layout.spinner_item,toLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                translateTv.setVisibility(View.VISIBLE);
                idSpeaker.setVisibility(View.VISIBLE);
                translateTv.setText("");
                targetActivity = new Intent();
                if (mInterstitialAd==null) startActivity(targetActivity);
                else if (FULLSCREEN_AD_LOAD_COUNT>=fullScreenAdMaxShowCount) startActivity(targetActivity);
                else mInterstitialAd.show(MainActivity.this);
                if(sourceEdittext.getText().toString().isEmpty()){

                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();


                }
                else if(toLanguageCode==0){

                    Toast.makeText(MainActivity.this, "Please select language to make translate", Toast.LENGTH_SHORT).show();

                }
                else{
                    translateText(fromLanguageCode,toLanguageCode,sourceEdittext.getText().toString());

                }
            }
        });

        micTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
        rateUsOnGooglePlay();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK & data !=null){

                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdittext.setText(result.get(0));
            }

        }
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source){
                translateTv.setText("Downloading model...");
        FirebaseTranslatorOptions options= new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
       FirebaseModelDownloadConditions conditions= new FirebaseModelDownloadConditions.Builder().build();
       translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void unused) {
               translateTv.setText("Translating...");
               translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                   @Override
                   public void onSuccess(String s) {
                    translateTv.setText(s);
                    idCopy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ClipboardManager clipboardManager= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData=ClipData.newPlainText("Text",translateTv.getText().toString());
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(MainActivity.this, "copy to clipboard", Toast.LENGTH_SHORT).show();

                        }
                    });
                    idShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent= new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, translateTv.getText().toString());
                            startActivity(Intent.createChooser(intent,"Share this"));
                        }
                    });
                    linearItemRateUs.setVisibility(View.VISIBLE);
                   }

               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, "Failed to translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(MainActivity.this, "Failed to download language model"+e.getMessage(), Toast.LENGTH_SHORT).show();
           }
       });

    }

//{"From","English","Afrikaans","Arabic","Bulgarian","Bengali","Catalan","Czech","Hindi","Urdu"}
    private int getLanguageCode(String language) {
        int languageCode=0;
        switch (language){
            case"English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;

            case"French":
                languageCode= FirebaseTranslateLanguage.FR;
                break;
            case"Japanese":
                languageCode= FirebaseTranslateLanguage.JA;
                break;
            case"Russian":
                languageCode= FirebaseTranslateLanguage.RU;
                break;
            case"Ukrainian":
                languageCode= FirebaseTranslateLanguage.UK;
                break;
            case"Chinese":
                languageCode= FirebaseTranslateLanguage.ZH;
                break;
            case"German":
                languageCode= FirebaseTranslateLanguage.DE;
                break;
            case"Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case"Sweden":
                languageCode= FirebaseTranslateLanguage.SW;
                break;
            case"Arabic":
                languageCode= FirebaseTranslateLanguage.AR;
                break;
            case"Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case"Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;
            case"Catalan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case"Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;
            case"Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;
            case"Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;
            default:
                languageCode=0;

        }
        return languageCode;
    }
    // for aD View function

    int BANNER_AD_CLICK_COUNT =0;
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void loadBannerAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                if (BANNER_AD_CLICK_COUNT >=3){
                    if(mAdView!=null) mAdView.setVisibility(View.GONE);
                }else{
                    if(mAdView!=null) mAdView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                BANNER_AD_CLICK_COUNT++;

                if (BANNER_AD_CLICK_COUNT >=3){
                    if(mAdView!=null) mAdView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    // loadFullscreenAd method starts here.....
    InterstitialAd mInterstitialAd;
    int FULLSCREEN_AD_LOAD_COUNT=0;
    private void loadFullscreenAd(){

        //Requesting for a fullscreen Ad
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,getString(R.string.admob_INTERSTITIAL_UNIT_ID), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;

                //Fullscreen callback || Requesting again when an ad is shown already
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        //User dismissed the previous ad. So we are requesting a new ad here
                        FULLSCREEN_AD_LOAD_COUNT++;
                        loadFullscreenAd();
                        Log.d("FULLSCREEN_AD", ""+FULLSCREEN_AD_LOAD_COUNT);

                        if (targetActivity!=null) startActivity(targetActivity);

                    }

                }); // FullScreen Callback Ends here


            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }

        });

    }


    // loadFullscreenAd method ENDS  here..... >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    private void initAdmobAd(){

        if (getString(R.string.device_id).length()>12){
            //Adding your device id -- to avoid invalid activity from your device
            List<String> testDeviceIds = Arrays.asList(getString(R.string.device_id));
            RequestConfiguration configuration =
                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }




        //Init Admob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

    }


    // for rating.....
    private void rateUsOnGooglePlay(){

        rLayRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }

            }
        });
    }

}