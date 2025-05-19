package com.wyldsoft.notes

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wyldsoft.notes.databinding.ActivityMainBinding
import com.wyldsoft.notes.pen.PenManager
import com.wyldsoft.notes.pen.StrokeProfile
import com.wyldsoft.notes.ui.PopupChangeAction
import com.wyldsoft.notes.ui.StrokeSettingsPopup

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var penManager: PenManager

    // Store the active button to track which profile is selected
    private var activeProfileButton: View? = null

    // Track if eraser mode is active
    private var eraserMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Initialize pen manager
        penManager = PenManager(this)

        // Set up canvas with pen manager
        binding.drawCanvas.initialize(penManager)

        // Initialize the toolbar buttons
        initToolbar()

        // Default to first profile (pencil)
        activateProfile(binding.btnPencil, StrokeProfile.PENCIL)
    }

    private fun initToolbar() {
        // Set up profile buttons
        binding.btnPencil.setOnClickListener { handleProfileButtonClick(it, StrokeProfile.PENCIL) }
        binding.btnBrush.setOnClickListener { handleProfileButtonClick(it, StrokeProfile.BRUSH) }
        binding.btnMarker.setOnClickListener { handleProfileButtonClick(it, StrokeProfile.MARKER) }
        binding.btnCharcoal.setOnClickListener { handleProfileButtonClick(it, StrokeProfile.CHARCOAL) }

        // Set up eraser button
        binding.btnEraser.setOnClickListener {
            eraserMode = !eraserMode
            if (eraserMode) {
                // Deactivate profile buttons and activate eraser
                activeProfileButton?.isSelected = false
                binding.btnEraser.isSelected = true
                penManager.setErasing(true)
            } else {
                // Reactivate last profile
                binding.btnEraser.isSelected = false
                activeProfileButton?.let { button ->
                    activateProfile(button, getProfileFromButton(button))
                }
            }
        }
    }

    private fun handleProfileButtonClick(view: View, profile: StrokeProfile) {
        if (view == activeProfileButton) {
            // If already active, show settings popup
            showStrokeSettings(view, profile)
        } else {
            // If not active, make it active
            activateProfile(view, profile)
        }
    }

    private fun activateProfile(button: View, profile: StrokeProfile) {
        // Deactivate eraser if active
        if (eraserMode) {
            eraserMode = false
            binding.btnEraser.isSelected = false
            penManager.setErasing(false)
        }

        // Deactivate previous profile button
        activeProfileButton?.isSelected = false

        // Activate new profile button
        button.isSelected = true
        activeProfileButton = button

        // Update pen manager with the new profile
        penManager.setStrokeProfile(profile)
    }

    private fun showStrokeSettings(anchorView: View, profile: StrokeProfile) {
        val popup = StrokeSettingsPopup(this, profile, penManager)
        popup.showAsDropDown(anchorView)
    }

    private fun getProfileFromButton(button: View): StrokeProfile {
        return when (button.id) {
            R.id.btn_pencil -> StrokeProfile.PENCIL
            R.id.btn_brush -> StrokeProfile.BRUSH
            R.id.btn_marker -> StrokeProfile.MARKER
            R.id.btn_charcoal -> StrokeProfile.CHARCOAL
            else -> StrokeProfile.PENCIL // Default
        }
    }

    override fun onDestroy() {
        binding.drawCanvas.release()
        super.onDestroy()
    }
}