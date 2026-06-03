package com.github.digitallyrefined.androidipcamera.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.digitallyrefined.androidipcamera.R
import com.github.digitallyrefined.androidipcamera.helpers.InputValidator
import com.github.digitallyrefined.androidipcamera.helpers.SecureStorage

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val secureStorage = SecureStorage(requireContext())

            // Configure username
            findPreference<EditTextPreference>("username")?.apply {
                text = secureStorage.getSecureString(SecureStorage.KEY_USERNAME, "")

                setOnPreferenceChangeListener { _, newValue ->
                    val username = newValue.toString()
                    if (username.isNotEmpty() && !InputValidator.isValidUsername(username)) {
                        Toast.makeText(requireContext(),
                            "Username must be 1-50 characters, letters/numbers/hyphens/underscores only",
                            Toast.LENGTH_LONG).show()
                        return@setOnPreferenceChangeListener false
                    }
                    secureStorage.putSecureString(SecureStorage.KEY_USERNAME, username)
                    true
                }
            }

            // Configure password
            findPreference<EditTextPreference>("password")?.apply {
                setOnBindEditTextListener { editText ->
                    editText.text = null
                    editText.hint = "Enter new password"
                }

                setOnPreferenceChangeListener { _, newValue ->
                    val password = newValue.toString()
                    if (password.isEmpty()) {
                        return@setOnPreferenceChangeListener false
                    }

                    if (!InputValidator.isValidPassword(password)) {
                        Toast.makeText(
                            requireContext(),
                            "Password must be 8-128 characters with uppercase, lowercase, and number",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnPreferenceChangeListener false
                    }

                    secureStorage.putSecureString(SecureStorage.KEY_PASSWORD, password)
                    false
                }
            }

            // Add validation for stream delay
            findPreference<EditTextPreference>("stream_delay")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val delay = newValue.toString()
                    if (!InputValidator.isValidStreamDelay(delay)) {
                        Toast.makeText(requireContext(),
                            "Frame delay must be between 10-1000 milliseconds",
                            Toast.LENGTH_LONG).show()
                        return@setOnPreferenceChangeListener false
                    }
                    true
                }
            }

            // Add listener for camera resolution changes
            findPreference<Preference>("camera_resolution")?.apply {
                setOnPreferenceChangeListener { preference, newValue ->
                    val resolution = newValue.toString()
                    if (!InputValidator.isValidCameraResolution(resolution)) {
                        Toast.makeText(requireContext(),
                            "Invalid camera resolution setting",
                            Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceChangeListener false
                    }

                    preferenceManager.sharedPreferences?.edit()?.apply {
                        putString("camera_resolution", resolution)
                        apply()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }, 500)

                    true
                }
            }

            // Add listeners for zoom and scale changes
            findPreference<Preference>("camera_zoom")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val zoom = newValue.toString()
                    val zoomFloat = zoom.toFloatOrNull()
                    if (zoomFloat == null || zoomFloat < 0.5f || zoomFloat > 2.0f) {
                        Toast.makeText(requireContext(),
                            "Zoom must be between 0.5x and 2.0x",
                            Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceChangeListener false
                    }

                    val intent = Intent("com.github.digitallyrefined.androidipcamera.RESTART_CAMERA").apply {
                        setPackage(requireContext().packageName)
                    }
                    requireContext().sendBroadcast(intent)
                    true
                }
            }

            findPreference<Preference>("stream_scale")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val scale = (newValue as? String)?.toFloatOrNull() ?: 1.0f
                    if (scale < 0.5f || scale > 2.0f) {
                        Toast.makeText(requireContext(),
                            "Scale must be between 0.5x and 2.0x",
                            Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceChangeListener false
                    }

                    val intent = Intent("com.github.digitallyrefined.androidipcamera.RESTART_CAMERA").apply {
                        setPackage(requireContext().packageName)
                    }
                    requireContext().sendBroadcast(intent)
                    true
                }
            }

            // Add listener for exposure changes
            findPreference<Preference>("camera_exposure")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val exposure = (newValue as? String)?.toIntOrNull() ?: 0
                    if (exposure < -2 || exposure > 2) {
                        Toast.makeText(requireContext(),
                            "Exposure must be between -2 and +2 EV",
                            Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceChangeListener false
                    }

                    val intent = Intent("com.github.digitallyrefined.androidipcamera.RESTART_CAMERA").apply {
                        setPackage(requireContext().packageName)
                    }
                    requireContext().sendBroadcast(intent)
                    true
                }
            }

            // Add listener for contrast changes
            findPreference<Preference>("camera_contrast")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val contrast = (newValue as? String)?.toIntOrNull() ?: 0
                    if (contrast < -50 || contrast > 50) {
                        Toast.makeText(requireContext(),
                            "Contrast must be between -50 and +50",
                            Toast.LENGTH_SHORT).show()
                        return@setOnPreferenceChangeListener false
                    }

                    val intent = Intent("com.github.digitallyrefined.androidipcamera.RESTART_CAMERA").apply {
                        setPackage(requireContext().packageName)
                    }
                    requireContext().sendBroadcast(intent)
                    true
                }
            }
        }
    }
}
