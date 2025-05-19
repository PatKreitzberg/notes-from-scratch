package com.wyldsoft.notes


import android.content.ContentValues.TAG
import android.graphics.Rect
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wyldsoft.notes.databinding.ActivityNotepadBinding
import com.wyldsoft.notes.pen.NotePenManager
import com.wyldsoft.notes.toolbar.PenSettingPopup
import com.onyx.android.sdk.pen.RawInputCallback
import java.util.*

class NotepadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotepadBinding
    private lateinit var notePenManager: NotePenManager
    private var selectedPenButton: ImageButton? = null
    private var penSettingPopup: PenSettingPopup? = null
    private val TAG = "NotepadActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notepad)

        initializePenManager()
        setupToolbar()
        binding.surfaceView.holder.addCallback(surfaceCallback)
    }

    private fun initializePenManager() {
        notePenManager = NotePenManager(this)

        // Waiting for surface view to be ready
        binding.surfaceView.post {
            val limitRect = Rect()
            binding.surfaceView.getLocalVisibleRect(limitRect)

            val excludeRects = ArrayList<Rect>()
            excludeRects.add(getRelativeRect(binding.surfaceView, binding.toolbarContainer))

            notePenManager.initialize(binding.surfaceView, limitRect, excludeRects)
            notePenManager.clearCanvas(binding.surfaceView)
        }
    }

    private fun setupToolbar() {
        // Setup pen buttons
        binding.buttonFountain.setOnClickListener { onPenButtonClick(it, NotePenManager.PEN_TYPE_FOUNTAIN) }
        binding.buttonBrush.setOnClickListener { onPenButtonClick(it, NotePenManager.PEN_TYPE_BRUSH) }
        binding.buttonPencil.setOnClickListener { onPenButtonClick(it, NotePenManager.PEN_TYPE_PENCIL) }
        binding.buttonCharcoal.setOnClickListener { onPenButtonClick(it, NotePenManager.PEN_TYPE_CHARCOAL) }
        binding.buttonMarker.setOnClickListener { onPenButtonClick(it, NotePenManager.PEN_TYPE_MARKER) }

        // Setup eraser button
        binding.buttonEraser.setOnClickListener {
            notePenManager.setErasing(true)
            updateSelectedButton(null)
        }

        // Set initial selection
        updateSelectedButton(binding.buttonFountain)
        notePenManager.setCurrentPenType(NotePenManager.PEN_TYPE_FOUNTAIN)
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            notePenManager.clearCanvas(binding.surfaceView)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            notePenManager.clearCanvas(binding.surfaceView)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Nothing to do here
        }
    }

    private fun onPenButtonClick(view: View, penType: Int) {
        val penButton = view as ImageButton

        if (penButton == selectedPenButton) {
            // Check if popup is already showing
            if (penSettingPopup?.isShowing == true) {
                penSettingPopup?.dismiss()
                return
            }

            // Create new popup if needed
            if (penSettingPopup == null) {
                penSettingPopup = PenSettingPopup(this, notePenManager)
                penSettingPopup?.setOnDismissListener {
                    // Add a small delay to allow input events to complete
                    binding.surfaceView.postDelayed({
                        penSettingPopup?.cleanup()
                    }, 100)
                }
            }

            try {
                penSettingPopup?.showAsDropDown(penButton)
            } catch (e: Exception) {
                e.printStackTrace()
                // Create a new popup instance if showing failed
                penSettingPopup?.cleanup()
                penSettingPopup = null
            }
        } else {
            // Switch to selected pen type
            notePenManager.setErasing(false)
            notePenManager.setCurrentPenType(penType)
            updateSelectedButton(penButton)
        }
    }


    private fun updateSelectedButton(button: ImageButton?) {
        // Reset previous selection
        selectedPenButton?.setBackgroundResource(R.drawable.bg_toolbar_button)

        // Update new selection
        selectedPenButton = button
        button?.setBackgroundResource(R.drawable.bg_toolbar_button_selected)
    }

    private fun getRelativeRect(parent: View, child: View): Rect {
        val parentLocation = IntArray(2)
        val childLocation = IntArray(2)

        parent.getLocationOnScreen(parentLocation)
        child.getLocationOnScreen(childLocation)

        val rect = Rect()
        child.getLocalVisibleRect(rect)

        rect.offset(childLocation[0] - parentLocation[0], childLocation[1] - parentLocation[1])
        return rect
    }

    override fun onDestroy() {
        penSettingPopup?.dismiss()
        penSettingPopup?.cleanup()
        penSettingPopup = null
        notePenManager.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        // Dismiss popup to prevent input receiver issues
        penSettingPopup?.dismiss()
        penSettingPopup = null

        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Reinitialize if needed
    }
}