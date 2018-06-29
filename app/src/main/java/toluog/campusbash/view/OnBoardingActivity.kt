package toluog.campusbash.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment

import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment

import toluog.campusbash.R
import toluog.campusbash.utils.Analytics

class OnBoardingActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance("Welcome to Campus Bash",
                getString(R.string.Description1Onboarding),
                R.drawable.mug, resources.getColor(R.color.colorPrimary)))
        addSlide(AppIntroFragment.newInstance("Find the hottest parties",
                getString(R.string.Description2Onboarding), R.drawable.dancer,
                resources.getColor(R.color.colorPrimary)))
        addSlide(AppIntroFragment.newInstance("Host the coolest parties",
                getString(R.string.Description3Onboarding), R.drawable.fire,
                resources.getColor(R.color.colorPrimary)))


        setBarColor(resources.getColor(R.color.colorPrimary))

        showSkipButton(true)
        isProgressButtonEnabled = true

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        val intent = Intent(this@OnBoardingActivity, FirstOpenActivity::class.java)
        startActivity(intent)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val intent = Intent(this@OnBoardingActivity, FirstOpenActivity::class.java)
        startActivity(intent)
        Analytics.logOnBoardFinished()
    }


    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }


}
