package com.wyldsoft.notes.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.wyldsoft.notes.R
import com.wyldsoft.notes.databinding.PopupStrokeSettingsBinding
import com.wyldsoft.notes.pen.PenManager
import com.wyldsoft.notes.pen.StrokeProfile

class StrokeSettingsPopup(
    private val context: Context,
    private val profile: StrokeProfile,
    private val penManager: PenManager
) : PopupWindow(context) {

    private val binding: PopupStrokeSettingsBinding

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.popup_stroke_settings,
            null,
            false
        )

        contentView = binding.root
        width = context.resources.getDimensionPixelSize(R.dimen.popup_width)
        height = context.resources.getDimensionPixelSize(R.dimen.popup_height)
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.WHITE))

        setupUI()
    }

    private fun setupUI() {
        // Set the title based on the profile
        binding.tvProfileTitle.text = when (profile) {
            StrokeProfile.PENCIL -> context.getString(R.string.pencil_settings)
            StrokeProfile.BRUSH -> context.getString(R.string.brush_settings)
            StrokeProfile.MARKER -> context.getString(R.string.marker_settings)
            StrokeProfile.CHARCOAL -> context.getString(R.string.charcoal_settings)
        }

        // Set up stroke width slider
        val currentWidth = penManager.getCurrentStrokeWidth()
        binding.seekBarStrokeWidth.progress = ((currentWidth - MIN_STROKE_WIDTH) / (MAX_STROKE_WIDTH - MIN_STROKE_WIDTH) * 100).toInt()
        binding.tvStrokeWidth.text = String.format("%.1f", currentWidth)

        binding.seekBarStrokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width = MIN_STROKE_WIDTH + (progress / 100f) * (MAX_STROKE_WIDTH - MIN_STROKE_WIDTH)
                binding.tvStrokeWidth.text = String.format("%.1f", width)
                if (fromUser) {
                    penManager.setStrokeWidth(width)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set up color selection
        val currentColor = penManager.getCurrentStrokeColor()
        when (currentColor) {
            Color.BLACK -> binding.colorBlack.isSelected = true
            Color.DKGRAY -> binding.colorDarkGray.isSelected = true
            Color.BLUE -> binding.colorBlue.isSelected = true
            Color.RED -> binding.colorRed.isSelected = true
        }

        binding.colorBlack.setOnClickListener { updateStrokeColor(Color.BLACK, it) }
        binding.colorDarkGray.setOnClickListener { updateStrokeColor(Color.DKGRAY, it) }
        binding.colorBlue.setOnClickListener { updateStrokeColor(Color.BLUE, it) }
        binding.colorRed.setOnClickListener { updateStrokeColor(Color.RED, it) }

        // Apply button
        binding.btnApply.setOnClickListener {
            dismiss()
        }
    }

    private fun updateStrokeColor(color: Int, selectedView: View) {
        // Deselect all color views
        binding.colorBlack.isSelected = false
        binding.colorDarkGray.isSelected = false
        binding.colorBlue.isSelected = false
        binding.colorRed.isSelected = false

        // Select the chosen color
        selectedView.isSelected = true

        // Update the pen manager
        penManager.setStrokeColor(color)
    }

    override fun showAsDropDown(anchor: View) {
        PopupChangeAction(true).execute()
        super.showAsDropDown(anchor)
    }

    override fun dismiss() {
        super.dismiss()
        PopupChangeAction(false).execute()
    }

    companion object {
        private const val MIN_STROKE_WIDTH = 1.0f
        private const val MAX_STROKE_WIDTH = 20.0f
    }
}