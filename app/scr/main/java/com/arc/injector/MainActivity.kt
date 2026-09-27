package com.arc.injector

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.arc.injector.data.ArcRepository
import com.arc.injector.data.Hero
import com.arc.injector.inject.ArcInjectorEngine
import com.arc.injector.inject.ShizukuInstaller
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var heroSpinner: Spinner
    private lateinit var sourceSpinner: Spinner
    private lateinit var targetSpinner: Spinner
    private lateinit var status: TextView
    private lateinit var injectButton: Button
    private lateinit var heroes: List<Hero>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        buildUi()
        loadData()

        Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_SHIZUKU) {
                status.text = if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)
                    "Shizuku permission granted."
                else "Shizuku permission denied."
                updateButton()
            }
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(0xFF0B0D12.toInt())
        }

        root.addView(TextView(this).apply {
            text = "ARC INJECTOR"
            textSize = 26f
            setTextColor(0xFFF4F5F7.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }, LinearLayout.LayoutParams(-1, dp(45)))

        root.addView(TextView(this).apply {
            text = "Skin replacement • Shizuku powered"
            textSize = 13f
            setTextColor(0xFF9AA3B2.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }, LinearLayout.LayoutParams(-1, dp(35)))

        heroSpinner = addSpinner(root, "Hero")
        sourceSpinner = addSpinner(root, "Current skin")
        targetSpinner = addSpinner(root, "Replacement skin")

        injectButton = Button(this).apply {
            text = "INJECT"
            isAllCaps = false
            setOnClickListener { inject() }
        }
        root.addView(injectButton, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(16) })

        root.addView(Button(this).apply {
            text = "Grant Shizuku permission"
            setOnClickListener { ShizukuInstaller.requestPermission(this@MainActivity, REQUEST_SHIZUKU) }
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })

        status = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF9AA3B2.toInt())
            setPadding(0, dp(16), 0, 0)
        }
        root.addView(status)

        heroSpinner.onItemSelectedListener = Listener { refreshSkins() }
        sourceSpinner.onItemSelectedListener = Listener { updateButton() }
        targetSpinner.onItemSelectedListener = Listener { updateButton() }

        setContentView(root)
    }

    private fun addSpinner(root: LinearLayout, label: String): Spinner {
        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        wrapper.addView(TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(0xFF9AA3B2.toInt())
            setPadding(0, dp(8), 0, dp(4))
        })
        return Spinner(this).also { spinner ->
            wrapper.addView(spinner, LinearLayout.LayoutParams(-1, dp(50)))
            root.addView(wrapper)
        }
    }

    private fun loadData() {
        try {
            heroes = ArcRepository(this).loadHeroes()
            heroSpinner.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, heroes.map { it.name }
            )
            refreshSkins()
            status.text = if (ShizukuInstaller.isReady()) "Ready." else "Shizuku permission required."
        } catch (t: Throwable) {
            heroes = emptyList()
            status.text = "Arc.json error: ${t.message}"
            updateButton()
        }
    }

    private fun refreshSkins() {
        if (heroes.isEmpty()) return
        val hero = heroes[heroSpinner.selectedItemPosition.coerceIn(0, heroes.lastIndex)]
        val names = hero.skins.map { it.name }
        sourceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        updateButton()
    }

    private fun updateButton() {
        if (heroes.isEmpty()) {
            injectButton.isEnabled = false
            return
        }
        val hero = heroes[heroSpinner.selectedItemPosition.coerceIn(0, heroes.lastIndex)]
        val source = hero.skins.getOrNull(sourceSpinner.selectedItemPosition)
        val target = hero.skins.getOrNull(targetSpinner.selectedItemPosition)
        val key = if (source != null && target != null) "${source.id}->${target.id}" else ""
        injectButton.isEnabled =
            source != null && target != null &&
            source.id != target.id &&
            !hero.packages[key].isNullOrBlank() &&
            ShizukuInstaller.isReady()
    }

    private fun inject() {
        if (heroes.isEmpty()) return
        val hero = heroes[heroSpinner.selectedItemPosition]
        val source = hero.skins[sourceSpinner.selectedItemPosition]
        val target = hero.skins[targetSpinner.selectedItemPosition]
        val url = hero.packages["${source.id}->${target.id}"].orEmpty()

        status.text = "Downloading and injecting..."
        injectButton.isEnabled = false

        Executors.newSingleThreadExecutor().execute {
            try {
                val message = ArcInjectorEngine(this).inject(
                    url,
                    "/storage/emulated/0/Android/data/com.mobile.legends"
                ) { progress ->
                    runOnUiThread { status.text = "Downloading: $progress%" }
                }
                runOnUiThread { status.text = "Done: $message"; updateButton() }
            } catch (t: Throwable) {
                runOnUiThread { status.text = "Failed: ${t.message}"; updateButton() }
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private class Listener(private val action: () -> Unit) :
        AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = action()
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    companion object {
        private const val REQUEST_SHIZUKU = 1001
    }
}
