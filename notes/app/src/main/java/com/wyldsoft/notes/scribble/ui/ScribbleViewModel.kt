package com.wyldsoft.notes.scribble.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*

class ScribbleViewModel : ViewModel() {

    // Pen profiles list
    private val _penProfiles = mutableStateOf(initializePenProfiles())
    val penProfiles: State<List<PenProfile>> = _penProfiles

    // Currently selected profile index
    private val _selectedProfileIndex = mutableStateOf(0)
    val selectedProfileIndex: State<Int> = _selectedProfileIndex

    // Current pen profile (derived from selected index)
    val currentPenProfile: State<PenProfile>
        get() = derivedStateOf {
            penProfiles.value[selectedProfileIndex.value]
        }

    // LiveData for Activity compatibility
    private val _currentPenProfileLiveData = MutableLiveData(penProfiles.value[0])
    val currentPenProfileLiveData: LiveData<PenProfile> = _currentPenProfileLiveData

    // Popup state
    private val _showProfileEditPopup = mutableStateOf(false)
    val showProfileEditPopup: State<Boolean> = _showProfileEditPopup

    private val _editingProfileIndex = mutableStateOf(-1)
    val editingProfileIndex: State<Int> = _editingProfileIndex

    // Drawing state
    private val _isDrawingEnabled = mutableStateOf(true)
    val isDrawingEnabled: State<Boolean> = _isDrawingEnabled

    // LiveData for Activity compatibility
    private val _isDrawingEnabledLiveData = MutableLiveData(true)
    val isDrawingEnabledLiveData: LiveData<Boolean> = _isDrawingEnabledLiveData

    private fun initializePenProfiles(): List<PenProfile> {
        return listOf(
            PenProfile.createCharcoalProfile(),
            PenProfile.createFountainProfile(),
            PenProfile.createMarkerProfile(),
            PenProfile.createNeoBrushProfile(),
            PenProfile.createPencilProfile()
        )
    }

    fun selectPenProfile(index: Int) {
        println("DEBUG: selectPenProfile called with index: $index, current selected: ${_selectedProfileIndex.value}")

        if (index < 0 || index >= penProfiles.value.size) return

        // If clicking the same profile, show edit popup
        if (_selectedProfileIndex.value == index) {
            println("DEBUG: Same profile clicked - showing popup for profile $index")
            showProfileEditPopup(index)
            return
        }

        println("DEBUG: Different profile selected - switching from ${_selectedProfileIndex.value} to $index")
        // Switch to different profile
        _selectedProfileIndex.value = index
        _currentPenProfileLiveData.value = penProfiles.value[index]
    }

    fun showProfileEditPopup(profileIndex: Int) {
        println("DEBUG: showProfileEditPopup called with index: $profileIndex")
        println("DEBUG: Current popup state before: ${_showProfileEditPopup.value}")
        println("DEBUG: Current editing index before: ${_editingProfileIndex.value}")

        _editingProfileIndex.value = profileIndex
        _showProfileEditPopup.value = true

        println("DEBUG: Popup state after setting: ${_showProfileEditPopup.value}")
        println("DEBUG: Editing index after setting: ${_editingProfileIndex.value}")

        pauseDrawing()
    }

    fun hideProfileEditPopup() {
        _showProfileEditPopup.value = false
        _editingProfileIndex.value = -1
        println("DEBUG: hideProfileEditPopup - resuming drawing")
        resumeDrawing()
    }

    fun updateProfile(profileIndex: Int, newProfile: PenProfile) {
        val updatedProfiles = penProfiles.value.toMutableList()
        updatedProfiles[profileIndex] = newProfile
        _penProfiles.value = updatedProfiles

        // Update LiveData if this is the current profile
        if (profileIndex == selectedProfileIndex.value) {
            _currentPenProfileLiveData.value = newProfile
            println("DEBUG: Updated current profile - LiveData synced")
        }
    }

    fun cancelProfileEdit() {
        hideProfileEditPopup()
    }

    private fun pauseDrawing() {
        _isDrawingEnabled.value = false
    }

    private fun resumeDrawing() {
        if (!_showProfileEditPopup.value) { // Only resume if no popup is showing
            _isDrawingEnabled.value = true
            println("DEBUG: Drawing resumed - isDrawingEnabled: ${_isDrawingEnabled.value}")
        }
    }

    fun getCurrentProfile(): PenProfile {
        return currentPenProfile.value
    }
}