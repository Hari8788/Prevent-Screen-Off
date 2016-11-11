/*
 * Copyright 2016 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevalpatel.preventscreenoff;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Keval on 27-Oct-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public abstract class AnalyserActivity extends AppCompatActivity implements ScreenListener {
    private FaceAnalyser mFaceAnalyser;
    private LightIntensityManager mLightIntensityManager;

    private boolean isForcedStop = false;

    @Override
    protected void onStart() {
        super.onStart();

        //initialize the face analysis
        mFaceAnalyser = new FaceAnalyser(this, addPreView());

        //initialize light intensity manager
        mLightIntensityManager = new LightIntensityManager(this);

        //start eye tracking
        startEyeTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start face tracking when application is foreground
        if (!isForcedStop && !mFaceAnalyser.isTrackingRunning()) startEyeTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop face tracking when application goes to background
        if (mFaceAnalyser.isTrackingRunning()) stopEyeTrackingInternal();
    }

    /**
     * Start the eye tracking and front camera.
     */
    public final void startEyeTracking() {
        if (mFaceAnalyser == null)
            throw new RuntimeException("Cannot start eye analysis in onCreate(). Start it in onStart().");

        isForcedStop = false;
        if (!mFaceAnalyser.isTrackingRunning()) {
            mFaceAnalyser.startEyeTracker();

            //start light monitoring
            mLightIntensityManager.startLightMonitoring();
        }
    }

    /**
     * Stop face analysis and release front camera.
     */
    public final void stopEyeTracking() {
        isForcedStop = true;
        stopEyeTrackingInternal();
    }

    /**
     * Stop face analysis and release front camera.
     */
    private void stopEyeTrackingInternal() {
        if (mFaceAnalyser.isTrackingRunning()) {
            mFaceAnalyser.stopEyeTracker();
            mLightIntensityManager.stopLightMonitoring();
        }
    }

    /**
     * This method will be called whenever eye analysis is stopped due to low light intensity.
     * This will stop eye tracking and publish error with error code  {@link Errors#LOW_LIGHT}.
     */
    void onLowLightIntensity(){
        stopEyeTrackingInternal();
        this.onErrorOccurred(Errors.LOW_LIGHT);
    }

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return {@link CameraSourcePreview}
     */
    private CameraSourcePreview addPreView() {
        //create fake camera view
        CameraSourcePreview cameraSourcePreview = new CameraSourcePreview(this);
        cameraSourcePreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = ((ViewGroup) getWindow().getDecorView().getRootView()).getChildAt(0);

        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 1);
            linearLayout.addView(cameraSourcePreview, params);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) view;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            relativeLayout.addView(cameraSourcePreview, params);
        } else if (view instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) view;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
            frameLayout.addView(cameraSourcePreview, params);
        } else {
            throw new RuntimeException("Root view of the activity/fragment cannot be frame layout");
        }

        return cameraSourcePreview;
    }
}
