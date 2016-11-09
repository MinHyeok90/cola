package com.example.android.cola;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro1_title), getString(R.string.intro1_desc),
                R.drawable.polaroid,
                Color.parseColor("#ffffff"), getColor(R.color.colorPrimary), getColor(R.color.colorPrimaryDark)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro2_title), getString(R.string.intro2_desc),
                R.drawable.friend,
                Color.parseColor("#ffffff"), getColor(R.color.colorPrimary), getColor(R.color.colorPrimaryDark)));

        setSkipTextTypeface(getResources().getString(R.string.skip));
        setColorSkipButton(Color.parseColor("#000000"));
        setDoneTextTypeface(getResources().getString(R.string.done));
        setColorDoneText(Color.parseColor("#000000"));
        setNextArrowColor(Color.parseColor("#000000"));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(this, AlbumsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(this, AlbumsActivity.class);
        startActivity(intent);
        finish();
    }
}
