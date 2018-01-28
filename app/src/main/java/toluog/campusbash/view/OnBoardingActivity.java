package toluog.campusbash.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import toluog.campusbash.R;

public class OnBoardingActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance("Welcome to Campus Bash",
                getString(R.string.Description1Onboarding),
                R.drawable.mug,getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Find the hottest parties",
                getString(R.string.Description2Onboarding),R.drawable.dancer,
                getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Host the coolest parties",
                getString(R.string.Description3Onboarding),R.drawable.fire,
                getResources().getColor(R.color.colorPrimary)));


        setBarColor(getResources().getColor(R.color.colorPrimary));

        showSkipButton(true);
        setProgressButtonEnabled(true);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment){
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(OnBoardingActivity.this,FirstOpenActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(OnBoardingActivity.this,FirstOpenActivity.class);
        startActivity(intent);
    }


    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }


}
