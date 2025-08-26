package com.example.routineapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class RoutineItem(val title: String, val done: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val itemsState = remember { mutableStateOf(loadItems(this)) }
                Surface(tonalElevation = 2.dp) {
                    AppUI(
                        items = itemsState.value,
                        onToggle = { idx, checked ->
                            val updated = itemsState.value.toMutableList()
                            updated[idx] = updated[idx].copy(done = checked)
                            itemsState.value = updated
                        },
                        onAdd = { text ->
                            val updated = itemsState.value.toMutableList()
                            updated.add(RoutineItem(text, false))
                            itemsState.value = updated
                        },
                        onSave = { saveItems(this, itemsState.value) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUI(items: List<RoutineItem>, onToggle: (Int, Boolean) -> Unit, onAdd: (String) -> Unit, onSave: () -> Unit) {
    var newText by remember { mutableStateOf("") }
    Scaffold(topBar = {
        TopAppBar(title = { Text("RoutineApp") }, actions = {
            val done = items.count { it.done }
            Text("Progreso: $done/${items.size}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
        })
    }) { inner ->
        Column(Modifier.padding(inner).padding(12.dp)) {
            Row {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("Nueva actividad / tarea") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { if (newText.isNotBlank()) { onAdd(newText.trim()); newText = "" } }) { Text("Agregar") }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(items.size) { idx ->
                    val it = items[idx]
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Checkbox(checked = it.done, onCheckedChange = { c -> onToggle(idx, c) })
                        Spacer(Modifier.width(8.dp))
                        Text(it.title)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSave) { Text("Guardar") }
        }
    }
}

private const val PREFS = "routine_prefs"
private const val KEY_ITEMS = "items"

fun loadItems(ctx: Context): List<RoutineItem> {
    val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val raw = sp.getString(KEY_ITEMS, null) ?: return seed()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            RoutineItem(o.getString("title"), o.optBoolean("done", false))
        }
    } catch (_: Exception) {
        seed()
    }
}

fun saveItems(ctx: Context, list: List<RoutineItem>) {
    val arr = JSONArray()
    list.forEach { item ->
        val o = JSONObject()
        o.put("title", item.title)
        o.put("done", item.done)
        arr.put(o)
    }
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString(KEY_ITEMS, arr.toString()).apply()
}

private fun seed(): List<RoutineItem> = listOf(
    RoutineItem("Levantarse 7:00am", false),
    RoutineItem("Estudio (2h) programación", false),
    RoutineItem("Trabajo 8am-3pm", false),
    RoutineItem("Pesas (full body) 40-60min", false),
    RoutineItem("Entrenamiento fútbol (decisiones)", false),
    RoutineItem("Higiene / Ordenar cuarto", false),
)
