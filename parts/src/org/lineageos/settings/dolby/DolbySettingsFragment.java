/*
 * Copyright (C) 2018,2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.dolby;

import android.media.AudioAttributes;
import android.media.AudioDeviceAttributes;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import java.util.Arrays;
import java.util.List;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

import org.lineageos.settings.R;

public class DolbySettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener, OnMainSwitchChangeListener {

    private static final String TAG = "DolbySettingsFragment";

    private static final AudioAttributes ATTRIBUTES_MEDIA = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();

    public static final String PREF_ENABLE = "dolby_enable";
    public static final String PREF_PROFILE = "dolby_profile";
    public static final String PREF_PRESET = "dolby_preset";
    public static final String PREF_VIRTUALIZER = "dolby_virtualizer";
    public static final String PREF_DIALOGUE = "dolby_dialogue";
    public static final String PREF_BASS = "dolby_bass";
    public static final String PREF_RESET = "dolby_reset";

    private MainSwitchPreference mSwitchBar;
    private ListPreference mProfilePref, mPresetPref, mVirtualizerPref, mDialoguePref;
    private SwitchPreference mBassPref;
    private Preference mResetPref;
    private CharSequence[] mPresets, mDeValues, mSwValues;

    private DolbyUtils mDolbyUtils;
    private AudioManager mAudioManager;
    private boolean mDsOn, mIsProfileUnknown, mIsOnSpeaker;
    private final Handler mHandler = new Handler();

    private final AudioDeviceCallback mAudioDeviceCallback = new AudioDeviceCallback() {
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            updateSpeakerState(false);
        }

        public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            updateSpeakerState(false);
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.dolby_settings);

        mAudioManager = getActivity().getSystemService(AudioManager.class);
        mDolbyUtils = DolbyUtils.getInstance(getActivity());
        mDsOn = mDolbyUtils.getDsOn();

        mSwitchBar = (MainSwitchPreference) findPreference(PREF_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(mDsOn);

        mProfilePref = (ListPreference) findPreference(PREF_PROFILE);
        mProfilePref.setOnPreferenceChangeListener(this);
        mProfilePref.setEnabled(mDsOn);

        final CharSequence[] profiles = mProfilePref.getEntryValues();
        final String profile = Integer.toString(mDolbyUtils.getProfile());
        if (Arrays.asList(profiles).contains(profile)) {
            mProfilePref.setSummary("%s");
            mProfilePref.setValue(profile);
        } else {
            mProfilePref.setSummary(getActivity().getString(R.string.dolby_unknown));
            mIsProfileUnknown = true;
        }

        mPresetPref = (ListPreference) findPreference(PREF_PRESET);
        mPresetPref.setOnPreferenceChangeListener(this);
        mPresets = mPresetPref.getEntryValues();

        mVirtualizerPref = (ListPreference) findPreference(PREF_VIRTUALIZER);
        mVirtualizerPref.setOnPreferenceChangeListener(this);
        mSwValues = mVirtualizerPref.getEntryValues();

        mDialoguePref = (ListPreference) findPreference(PREF_DIALOGUE);
        mDialoguePref.setOnPreferenceChangeListener(this);
        mDeValues = mDialoguePref.getEntryValues();

        mBassPref = (SwitchPreference) findPreference(PREF_BASS);
        mBassPref.setOnPreferenceChangeListener(this);

        mResetPref = (Preference) findPreference(PREF_RESET);
        mResetPref.setEnabled(mDsOn && !mIsProfileUnknown);
        mResetPref.setOnPreferenceClickListener(p -> {
            mDolbyUtils.resetProfileSpecificSettings();
            updateProfileSpecificPrefs();
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.dolby_reset_profile_toast,
                            mProfilePref.getSummary()), Toast.LENGTH_SHORT).show();
            return true;
        });

        mAudioManager.registerAudioDeviceCallback(mAudioDeviceCallback, mHandler);
        updateSpeakerState(true);
    }

    @Override
    public void onDestroyView() {
        mAudioManager.unregisterAudioDeviceCallback(mAudioDeviceCallback);
        super.onDestroyView();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PREF_PROFILE:
                mDolbyUtils.setProfile(Integer.parseInt((newValue.toString())));
                mIsProfileUnknown = false;
                updateProfileSpecificPrefs();
                return true;
            case PREF_PRESET:
                mDolbyUtils.setPreset(newValue.toString());
                return true;
            case PREF_VIRTUALIZER:
                mDolbyUtils.setStereoWideningAmount(Integer.parseInt((newValue.toString())));
                return true;
            case PREF_DIALOGUE:
                mDolbyUtils.setDialogueEnhancerAmount(Integer.parseInt((newValue.toString())));
                return true;
            case PREF_BASS:
                mDolbyUtils.setBassEnhancerEnabled((Boolean) newValue);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        mDsOn = isChecked;
        mDolbyUtils.setDsOn(isChecked);
        mProfilePref.setEnabled(isChecked);
        mResetPref.setEnabled(isChecked);
        updateProfileSpecificPrefs();
    }

    private void updateSpeakerState(boolean force) {
        final AudioDeviceAttributes device =
                mAudioManager.getDevicesForAttributes(ATTRIBUTES_MEDIA).get(0);
        final boolean isOnSpeaker = (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
        if (mIsOnSpeaker != isOnSpeaker || force) {
            Log.d(TAG, "updateSpeakerState: " + mIsOnSpeaker);
            mIsOnSpeaker = isOnSpeaker;
            updateProfileSpecificPrefs();
        }
    }

    private void updateProfileSpecificPrefs() {
        final String unknown = getActivity().getString(R.string.dolby_unknown);
        final String speaker = getActivity().getString(R.string.dolby_connect_headphones);

        Log.d(TAG, "updateProfileSpecificPrefs: mDsOn=" + mDsOn
                + " mIsProfileUnknown=" + mIsProfileUnknown + " mIsOnSpeaker=" + mIsOnSpeaker);

        final boolean enable = mDsOn && !mIsProfileUnknown;
        mPresetPref.setEnabled(enable);
        mDialoguePref.setEnabled(enable);
        mVirtualizerPref.setEnabled(enable && !mIsOnSpeaker);
        mBassPref.setEnabled(enable && !mIsOnSpeaker);

        if (!enable) return;

        final String preset = mDolbyUtils.getPreset();
        if (Arrays.asList(mPresets).contains(preset)) {
            mPresetPref.setSummary("%s");
            mPresetPref.setValue(preset);
        } else {
            mPresetPref.setSummary(unknown);
        }

        final String deValue = Integer.toString(mDolbyUtils.getDialogueEnhancerAmount());
        if (Arrays.asList(mDeValues).contains(deValue)) {
            mDialoguePref.setSummary("%s");
            mDialoguePref.setValue(deValue);
        } else {
            mDialoguePref.setSummary(unknown);
        }

        if (mIsOnSpeaker) {
            mVirtualizerPref.setSummary(speaker);
            mBassPref.setSummary(speaker);
            return;
        }

        final String swValue = Integer.toString(mDolbyUtils.getStereoWideningAmount());
        if (Arrays.asList(mSwValues).contains(swValue)) {
            mVirtualizerPref.setSummary("%s");
            mVirtualizerPref.setValue(swValue);
        } else {
            mVirtualizerPref.setSummary(unknown);
        }

        mBassPref.setChecked(mDolbyUtils.getBassEnhancerEnabled());
        mBassPref.setSummary(null);
    }
}
