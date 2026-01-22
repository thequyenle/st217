package com.ocmaker.pony.dialog

import android.app.Activity
import android.graphics.Color
import android.widget.LinearLayout
import com.ocmaker.pony.core.extensions.gone
import com.ocmaker.pony.core.extensions.hideNavigation
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.R
import com.ocmaker.pony.core.base.BaseDialog
import com.ocmaker.pony.core.extensions.strings
import com.ocmaker.pony.databinding.DialogConfirmBinding

enum class DialogType {
    DELETE_EXIT,
    RESET,
    LOADING,
    INTERNET
}

class YesNoDialog(
    val context: Activity,
    val title: Int,
    val description: Int,
    val isError: Boolean = false,
    val dialogType: DialogType = DialogType.DELETE_EXIT
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        initText()
        initBackground()
        if (isError) {
            binding.btnNo.gone()
        }
        context.hideNavigation()
        binding.tvTitle.isSelected = true
    }

    private fun initBackground() {
        val bgRes = when (dialogType) {
            DialogType.DELETE_EXIT -> R.drawable.bg_dialog_delete_exit
            DialogType.RESET -> R.drawable.bg_dialog_reset
            DialogType.LOADING -> R.drawable.bg_dialog_loading
            DialogType.INTERNET -> R.drawable.bg_dialog_internet
        }
        binding.containerDialog.setBackgroundResource(bgRes)

        val textColor = when (dialogType) {
            DialogType.DELETE_EXIT -> Color.parseColor("#FF008C")
            DialogType.RESET -> Color.parseColor("#2AABEE")
            DialogType.LOADING -> Color.parseColor("#AB5BFF")
            DialogType.INTERNET -> Color.parseColor("#FE8700")
        }
        binding.tvDescription.setTextColor(textColor)
        binding.btnNo.setTextColor(textColor)

        when (dialogType) {
            DialogType.LOADING, DialogType.INTERNET -> {
                binding.btnNo.gone()
                (binding.btnYes.layoutParams as LinearLayout.LayoutParams).marginStart = 0
            }
            else -> {}
        }
    }

    override fun initAction() {
        binding.apply {
            btnNo.tap { onNoClick.invoke() }
            btnYes.tap { onYesClick.invoke() }
            flOutSide.tap { onDismissClick.invoke() }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
            // Use "Ok" text for error dialogs (like internet check)
            if (isError) {
                btnYes.text = context.strings(R.string.ok)
            }
        }
    }
}