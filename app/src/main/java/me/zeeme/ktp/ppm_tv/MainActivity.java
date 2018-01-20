package me.zeeme.ktp.ppm_tv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private boolean playWhenReady = true;
    private Handler mainHandler;
    private Context context;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.activity_video_linear);
                playerView = findViewById(R.id.my_exoplayer);


                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.activity_video_landscape);
                playerView = findViewById(R.id.exoplayer);
                hideSystemUi();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
        }

        //setContentView(R.layout.activity_video_linear);

        mainHandler = new Handler();


    }


    // ------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    //-------------------------------------------------------------


    private void initializePlayer() {


        if (player == null) {

            ///////////////////////////////////////////////////////////////////////////////////////////
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            String drmLicenseUrl = "https://proxy.uat.widevine.com/proxy?provider=widevine_test";
            UUID drmSchemeUuid = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed");

            String[] keyRequestPropertiesArray = null;
            String userAgent;
            userAgent = Util.getUserAgent(this, "Sample ExoPlayer");
            HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);

            try {
                HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(drmLicenseUrl, httpDataSourceFactory);
                if (keyRequestPropertiesArray != null) {
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }

                try {
                    drmSessionManager = new DefaultDrmSessionManager<>(drmSchemeUuid, FrameworkMediaDrm.newInstance(drmSchemeUuid), drmCallback,
                            null, mainHandler, null, false);
                } catch (UnsupportedDrmException e) {
                    Toast.makeText(getApplicationContext(), e.reason, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
            //    HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,   buildHttpDataSourceFactory(false));


            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this, drmSessionManager);


            ///////////////////////////////////////////////////////////////////////////////////////////


            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);


            // let the factory create a player instance with default components
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
            //  player = ExoPlayerFactory.newSimpleInstance(renderersFactory, new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());

            playerView.setPlayer(player);

            //    playerView.setRepeatToggleModes(1);
            player.setPlayWhenReady(playWhenReady);
            //player.seekTo(currentWindow, playbackPosition);
        }

        // return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);

        // MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash_wv)));

//----------------------------------------------------------------------------------------------------

        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        DefaultDataSourceFactory dataSourceFactory_hls = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "PPM_TV"), bandwidthMeterA);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // MediaSource videoSource_hls = new HlsMediaSource(Uri.parse(getString(R.string.media_url_aes_hls_ts)), dataSourceFactory_hls, 1, null, null);
        MediaSource videoSource_hls = new HlsMediaSource(Uri.parse(getString(R.string.media_url_hls)), dataSourceFactory_hls, 1, null, null);

        player.prepare(videoSource_hls, true, false);
        // player.prepare(mediaSource, true, false);

    }

    private void releasePlayer() {
        long playbackPosition;
        int currentWindow;
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            //  closeFullscreenDialog();
            // super.onBackPressed();
            //   showSystemUi();
            Log.d("tag", "Portrait");
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  initFullscreenDialog();
            //  initFullscreenButton();
            //  openFullscreenDialog();
            // hideSystemUi();
            Log.d("tag", "Landscape");
        } else
            Log.w("tag", "other: " + orientation);

    }
}


