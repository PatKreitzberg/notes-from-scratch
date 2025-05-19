package com.wyldsoft.notes.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wyldsoft.notes.R
import com.wyldsoft.notes.databinding.PopupPenSettingsBinding
import com.wyldsoft.notes.pen.NotePenManager

class PenSettingPopup(
    private val context: Context,
    private val penManager: NotePenManager
) : PopupWindow(context) {

    private var isDismissing = false
    private val binding: PopupPenSettingsBinding

    override fun dismiss() {
        if (isDismissing) return

        isDismissing = true
        try {
            super.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isDismissing = false
        }
    }

    fun cleanup() {
        println("penSettingPopoup cleanup()")
        setOnDismissListener(null)
        // Remove any other listeners that could be causing issues
    }

    // Pen types available
    private val penTypes = listOf(
        PenType(NotePenManager.PEN_TYPE_FOUNTAIN, R.drawable.ic_pen_fountain, "Fountain Pen"),
        PenType(NotePenManager.PEN_TYPE_BRUSH, R.drawable.ic_pen_soft, "Brush Pen"),
        PenType(NotePenManager.PEN_TYPE_PENCIL, R.drawable.ic_pen_hard, "Pencil"),
        PenType(NotePenManager.PEN_TYPE_CHARCOAL, R.drawable.ic_charcoal_pen, "Charcoal"),
        PenType(NotePenManager.PEN_TYPE_MARKER, R.drawable.ic_marker_pen, "Marker")
    )

    // Color options
    private val colorOptions = listOf(
        ColorOption(Color.BLACK, "Black"),
        ColorOption(Color.DKGRAY, "Dark Gray"),
        ColorOption(Color.RED, "Red"),
        ColorOption(Color.BLUE, "Blue"),
        ColorOption(Color.GREEN, "Green")
    )

    // Stroke width range
    private val minStrokeWidth = 1.0f
    private val maxStrokeWidth = 10.0f

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.popup_pen_settings,
            null,
            false
        )

        contentView = binding.root
        width = context.resources.getDimensionPixelSize(R.dimen.pen_popup_width)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 10f

        setupPenTypeRecyclerView()
        setupColorRecyclerView()
        setupStrokeWidthSlider()
    }

    private fun setupPenTypeRecyclerView() {
        binding.recyclerViewPenTypes.apply {
            layoutManager = GridLayoutManager(context, penTypes.size)
            adapter = PenTypeAdapter(penTypes) { penType ->
                penManager.setCurrentPenType(penType.id)
                dismiss()
            }
        }
    }

    private fun setupColorRecyclerView() {
        binding.recyclerViewColors.apply {
            layoutManager = GridLayoutManager(context, colorOptions.size)
            adapter = ColorAdapter(colorOptions) { colorOption ->
                penManager.setStrokeColor(colorOption.color)
            }
        }
    }

    private fun setupStrokeWidthSlider() {
        binding.sliderStrokeWidth.apply {
            max = 100
            progress = 30 // Default progress

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val strokeWidth = minStrokeWidth + (progress / 100f) * (maxStrokeWidth - minStrokeWidth)
                    penManager.setStrokeWidth(strokeWidth)
                    binding.textViewStrokeWidth.text = String.format("%.1f", strokeWidth)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Initial value
        val initialStrokeWidth = minStrokeWidth + (binding.sliderStrokeWidth.progress / 100f) *
                (maxStrokeWidth - minStrokeWidth)
        binding.textViewStrokeWidth.text = String.format("%.1f", initialStrokeWidth)
    }

    // Data classes
    data class PenType(val id: Int, val iconRes: Int, val name: String)
    data class ColorOption(val color: Int, val name: String)

    // Adapters
    inner class PenTypeAdapter(
        private val penTypes: List<PenType>,
        private val onItemClick: (PenType) -> Unit
    ) : RecyclerView.Adapter<PenTypeAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.image_pen_type)
            val radioButton: RadioButton = itemView.findViewById(R.id.radio_pen_type)

            init {
                itemView.setOnClickListener {
                    onItemClick(penTypes[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pen_type, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val penType = penTypes[position]
            holder.icon.setImageResource(penType.iconRes)
            holder.radioButton.text = penType.name
            holder.radioButton.isChecked = penManager.getCurrentPenType() == penType.id
        }

        override fun getItemCount() = penTypes.size
    }

    inner class ColorAdapter(
        private val colorOptions: List<ColorOption>,
        private val onItemClick: (ColorOption) -> Unit
    ) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val colorView: View = itemView.findViewById(R.id.view_color)
            val radioButton: RadioButton = itemView.findViewById(R.id.radio_color)

            init {
                itemView.setOnClickListener {
                    onItemClick(colorOptions[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_color, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val colorOption = colorOptions[position]
            holder.colorView.setBackgroundColor(colorOption.color)
            holder.radioButton.text = colorOption.name
            holder.radioButton.isChecked = penManager.getCurrentColor() == colorOption.color
        }

        override fun getItemCount() = colorOptions.size
    }
}