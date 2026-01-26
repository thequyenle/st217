package com.ocmaker.pony.ui.success

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ocmaker.pony.R
import com.ocmaker.pony.core.base.BaseActivity
import com.ocmaker.pony.core.extensions.checkPermissions
import com.ocmaker.pony.core.extensions.goToSettings
import com.ocmaker.pony.core.extensions.gone
import com.ocmaker.pony.core.extensions.handleBackLeftToRight
import com.ocmaker.pony.core.extensions.invisible
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.requestPermission
import com.ocmaker.pony.core.extensions.select
import com.ocmaker.pony.core.extensions.setImageActionBar
import com.ocmaker.pony.core.extensions.setTextActionBar
import com.ocmaker.pony.core.extensions.showInterAll
import com.ocmaker.pony.core.extensions.startIntentRightToLeft
import com.ocmaker.pony.core.extensions.startIntentWithClearTop
import com.ocmaker.pony.core.extensions.strings
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.core.extensions.visible
import com.ocmaker.pony.core.helper.UnitHelper
import com.ocmaker.pony.core.utils.key.IntentKey
import com.ocmaker.pony.core.utils.key.RequestKey
import com.ocmaker.pony.core.utils.state.HandleState
import com.ocmaker.pony.databinding.ActivitySuccessBinding
import com.ocmaker.pony.ui.home.HomeActivity
import com.ocmaker.pony.ui.my_creation.MyCreationActivity
import com.ocmaker.pony.ui.permission.PermissionViewModel
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    private val viewModel: SuccessViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
        setButtonBackgrounds()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            
            tvMyWork.select()
            tvDownload.select()
            tvShare.select()

        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pathInternal.collect { path ->
                        if (path.isNotEmpty()) {
                            loadImage(this@SuccessActivity, path, binding.imvImage)
                        }
                    }
                }
            }
        }
    }

    private fun handleBack() {
        handleBackLeftToRight()
    }
    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarRight.tap {
                    showInterAll {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }
                btnActionBarLeft.tap { showInterAll { handleBack() } }

            }

            // My Album button
            includeLayoutBottom.btnWhatsapp.tap(2590) {
                showInterAll {
                    startIntentRightToLeft(MyCreationActivity::class.java, true)
                }
            }

            // Download button
            includeLayoutBottom.btnTelegram.tap(2000) {
                checkStoragePermission()
            }
            includeLayoutBottom.btnShare.tap(2000){
                    viewModel.shareFiles(this@SuccessActivity)
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setTextActionBar(tvCenter, getString(R.string.successfully))
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            tvCenter.visible()
            imgCenter.gone()
                setImageActionBar(btnActionBarRight, R.drawable.ic_home_ss)
            btnActionBarNextRight.invisible()
            tvCenter.select()

        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@SuccessActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }
                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }
}
