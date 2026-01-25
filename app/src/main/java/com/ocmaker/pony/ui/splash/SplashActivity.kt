package com.ocmaker.pony.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.ocmaker.pony.R
import com.ocmaker.pony.core.base.BaseActivity
import com.ocmaker.pony.core.utils.state.HandleState
import com.ocmaker.pony.databinding.ActivitySplashBinding
import com.ocmaker.pony.ui.intro.IntroActivity
import com.ocmaker.pony.ui.language.LanguageActivity
import com.ocmaker.pony.ui.home.DataViewModel
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var intentActivity: Intent? = null
    private val dataViewModel: DataViewModel by viewModels()
    var interCallBack: InterCallback? = null

    private val MIN_SPLASH_MS = 3000L
    private var minTimePassed = false
    private var dataReady = false
    private var triggered = false

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Start loading animation
        val rotateAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rotate_loading)
        binding.ivLoading.startAnimation(rotateAnimation)

        Admob.getInstance().setOpenShowAllAds(false)
        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }

        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
        Admob.getInstance().setTimeLimitShowAds(30000)
        Admob.getInstance().setOpenShowAllAds(false)
        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(intentActivity)
                finishAffinity()
            }
        }
        dataViewModel.ensureData(this)

        lifecycleScope.launch {
            kotlinx.coroutines.delay(MIN_SPLASH_MS)
            minTimePassed = true
            tryProceed()
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { dataList ->
                if (dataList.isNotEmpty()){
                    dataViewModel.getAllParts(this@SplashActivity).collect { dataAPI ->
                        when(dataAPI){
                            HandleState.LOADING -> {}
                            else -> {
                                dataReady = true
                                tryProceed()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryProceed() {
        if (triggered) return
        if (!minTimePassed || !dataReady) return

        triggered = true
        Admob.getInstance().loadSplashInterAds(
            this@SplashActivity,
            getString(R.string.inter_splash),
            30000,
            2000,
            interCallBack
        )
    }

    override fun viewListener() {
    }

    override fun initText() {}

    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}

    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }
}