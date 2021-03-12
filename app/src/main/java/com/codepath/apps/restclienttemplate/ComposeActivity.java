package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    ImageView ivProfileImage;
    TextView tvName;
    TextView tvScreenName;
    EditText etCompose;
    TextView tvCharsRemaining;
    Button btnTweet;

    TwitterClient client;

    @SuppressLint ("DefaultLocale")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        setupActionBar();

        client = TwitterApp.getRestClient(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        etCompose = findViewById(R.id.etCompose);
        tvCharsRemaining = findViewById(R.id.tvCharsRemaining);
        btnTweet = findViewById(R.id.btnTweet);

        client.getLoggedInUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess (int statusCode, Headers headers, JSON json) {
                try {
                    setUserInfo(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure (int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Failed to get user info ", throwable);
            }
        });

        tvCharsRemaining.setText(String.format("%d", MAX_TWEET_LENGTH));

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                int rem = MAX_TWEET_LENGTH - charSequence.length();
                tvCharsRemaining.setText(rem >= 0 ?
                                         String.format("%d", rem) : "0");
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getApplicationContext(), "Sorry, your tweet is too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess (int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says " + tweet);

                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure (int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet: " + throwable.getMessage(), throwable);
                    }
                });
            }
        });
    }

    private void setUserInfo (JsonHttpResponseHandler.JSON json) throws JSONException {
        client.getCurrentUserInfo(json.jsonObject.getString("id"), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess (int statusCode, Headers headers, JSON json) {
                try {
                    User user = User.fromJson(json.jsonObject);
                    Glide.with(getApplicationContext()).load(user.profileImageUrl).transform(new RoundedCorners(15)).into(ivProfileImage);
                    tvName.setText(user.name);
                    tvScreenName.setText(user.screenName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure (int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Failed to get user info ", throwable);
            }
        });
    }

    private void setupActionBar() {
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayShowTitleEnabled(false);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#00ACEE"));
        bar.setBackgroundDrawable(colorDrawable);
        bar.setDisplayShowHomeEnabled(true);
        bar.setLogo(R.drawable.ic_logo_white);
        bar.setDisplayUseLogoEnabled(true);
    }
}