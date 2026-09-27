package com.arc.injector.data

import android.content.Context
import org.json.JSONObject

class ArcRepository(private val context: Context) {
    fun loadHeroes(): List<Hero> {
        val raw = context.assets.open("arc.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val array = root.optJSONArray("heroes") ?: return emptyList()

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val skins = buildList {
                    val a = obj.optJSONArray("skins") ?: return@buildList
                    for (j in 0 until a.length()) {
                        val s = a.getJSONObject(j)
                        add(Skin(s.getString("id"), s.getString("name")))
                    }
                }
                val packages = mutableMapOf<String, String>()
                obj.optJSONObject("packages")?.let { p ->
                    for (key in p.keys()) packages[key] = p.optString(key, "")
                }
                add(Hero(obj.getString("id"), obj.getString("name"), skins, packages))
            }
        }
    }
}
