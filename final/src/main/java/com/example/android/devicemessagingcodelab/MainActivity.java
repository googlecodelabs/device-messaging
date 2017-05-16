/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.devicemessagingcodelab;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Main UI activity for our messaging app
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final long DELAY_MILLIS = TimeUnit.SECONDS.toMillis(10);
    public static final long COUNT_DOWN_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private TextView mTextView;

    private CountDownTimer mCountDownTimer;
    private TextView mAutoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAutoText = (TextView) findViewById(R.id.help_text);
        mTextView = (TextView) findViewById(R.id.context_text);
        Button mStartButton = (Button) findViewById(R.id.start_conversation);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCountDownTimer();
            }
        });
    }

    private void startCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(DELAY_MILLIS, COUNT_DOWN_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                mAutoText.setVisibility(View.VISIBLE);
                int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                mTextView.setText(getResources().getQuantityString(R.plurals.post_countdown,
                        seconds, seconds));
            }

            public void onFinish() {
                mAutoText.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.post_countdown_complete));
                sendMessage();
            }
        };
        mCountDownTimer.start();
    }

    private void sendMessage() {
        Intent serviceIntent = new Intent(MainActivity.this, MessagingService.class);
        serviceIntent.setAction(MessagingService.SEND_MESSAGE_ACTION);
        startService(serviceIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
