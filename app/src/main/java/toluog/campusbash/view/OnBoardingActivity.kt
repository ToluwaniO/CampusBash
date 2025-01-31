package toluog.campusbash.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import kotlinx.coroutines.Dispatchers

import toluog.campusbash.R
import toluog.campusbash.data.datasource.UniversityDataSource
import toluog.campusbash.utils.Analytics

class OnBoardingActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UniversityDataSource(applicationContext, Dispatchers.Default).listenToUniversities()
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_to_cbash),
                getString(R.string.Description1Onboarding),
                R.drawable.mug, resources.getColor(R.color.colorPrimary)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.find_hottest_parties),
                getString(R.string.Description2Onboarding), R.drawable.dancer,
                resources.getColor(R.color.colorPrimary)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.host_coolest_parties),
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
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val intent = Intent(this@OnBoardingActivity, FirstOpenActivity::class.java)
        startActivity(intent)
        Analytics.logOnBoardFinished()
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }


}
