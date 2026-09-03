package com.example.novanotes

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var notesContainer: LinearLayout
    private lateinit var input: EditText
    private val preferences by lazy { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }
    private val notes = mutableListOf<Note>()
    private var nextId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        loadNotes()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(16))
            setBackgroundColor(Color.rgb(248, 247, 252))
        }

        val title = TextView(this).apply {
            text = "✦ NovaNotes"
            textSize = 30f
            setTextColor(Color.rgb(35, 30, 45))
            setPadding(0, 0, 0, dp(16))
        }
        root.addView(title, matchWrap())

        input = EditText(this).apply {
            hint = "Escribe una nota..."
            textSize = 17f
            setSingleLine(false)
            minLines = 2
            maxLines = 6
            gravity = Gravity.TOP
            setPadding(dp(12), dp(10), dp(12), dp(10))
            contentDescription = "Texto de la nota"
        }
        root.addView(input, matchWrap())

        val addButton = Button(this).apply {
            text = "Guardar nota"
            contentDescription = "Guardar nota"
            setOnClickListener { addNote() }
        }
        root.addView(addButton, matchWrap())

        notesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(16), 0, 0)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(notesContainer)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun addNote() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) {
            input.error = "Escribe algo antes de guardar"
            return
        }

        val note = Note(nextId++, text)
        notes.add(note)
        saveNotes()
        addNoteCard(note)
        input.text.clear()
    }

    private fun loadNotes() {
        val stored = preferences.getString(NOTES_JSON_KEY, null)
        if (stored != null) {
            runCatching {
                val array = JSONArray(stored)
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    notes += Note(item.getLong("id"), item.getString("text"))
                }
            }.onFailure {
                // A damaged preference should not crash the app.
                notes.clear()
            }
        } else {
            // Migrate data created by the original version of NovaNotes.
            preferences.getStringSet(LEGACY_ITEMS_KEY, emptySet()).orEmpty().forEach { text ->
                notes += Note(nextId++, text)
            }
            if (notes.isNotEmpty()) saveNotes()
        }

        nextId = (notes.maxOfOrNull { it.id } ?: -1L) + 1
        notes.forEach(::addNoteCard)
    }

    private fun addNoteCard(note: Note) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(6), dp(10))
            setBackgroundColor(Color.WHITE)
        }

        val label = TextView(this).apply {
            text = note.text
            textSize = 17f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, dp(8), 0)
        }
        val deleteButton = Button(this).apply {
            text = "×"
            textSize = 20f
            contentDescription = "Eliminar nota"
            setOnClickListener {
                notes.removeAll { it.id == note.id }
                notesContainer.removeView(card)
                saveNotes()
            }
        }

        card.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(deleteButton, LinearLayout.LayoutParams(dp(52), dp(52)))

        val cardParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(10))
        }
        notesContainer.addView(card, cardParams)
    }

    private fun saveNotes() {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id", note.id)
                put("text", note.text)
            })
        }
        preferences.edit().putString(NOTES_JSON_KEY, array.toString()).apply()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun matchWrap() = ViewGroup.LayoutParams(-1, -2)

    private data class Note(val id: Long, val text: String)

    private companion object {
        const val PREFERENCES_NAME = "notes"
        const val NOTES_JSON_KEY = "items_json"
        const val LEGACY_ITEMS_KEY = "items"
    }
}
