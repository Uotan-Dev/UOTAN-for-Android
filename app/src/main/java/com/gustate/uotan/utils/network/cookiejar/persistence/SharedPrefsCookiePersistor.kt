/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gustate.uotan.utils.network.cookiejar.persistence

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import androidx.core.content.edit

class SharedPrefsCookiePersistor private constructor(
    private val sharedPreferences: SharedPreferences
) : CookiePersistor {

    constructor(context: Context) : this(
        context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE)
    )

    override fun loadAll(): List<Cookie> {
        return sharedPreferences.all.values.mapNotNull { serializedCookie ->
            (serializedCookie as? String)?.let {
                SerializableCookie().decode(it)
            }
        }
    }

    override fun saveAll(cookies: Collection<Cookie>) {
        sharedPreferences.edit {
            cookies.forEach { cookie ->
                putString(createCookieKey(cookie), SerializableCookie().encode(cookie))
            }
        }
    }

    override fun removeAll(cookies: Collection<Cookie>) {
        sharedPreferences.edit {
            cookies.forEach { cookie ->
                remove(createCookieKey(cookie))
            }
        }
    }

    private fun createCookieKey(cookie: Cookie): String {
        return "${if (cookie.secure) "https" else "http"}://${cookie.domain}${cookie.path}|${cookie.name}"
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}