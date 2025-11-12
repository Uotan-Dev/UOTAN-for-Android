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

package com.gustate.uotan.utils.network.cookiejar

import com.gustate.uotan.utils.network.cookiejar.cache.SetCookieCache
import com.gustate.uotan.utils.network.cookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Cookie
import okhttp3.HttpUrl

class PersistentCookieJar(
    private val cache: SetCookieCache,
    private val persistor: SharedPrefsCookiePersistor
) : ClearableCookieJar {

    init {
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.addAll(cookies)
        persistor.saveAll(cookies)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesToRemove = mutableListOf<Cookie>()
        val validCookies = mutableListOf<Cookie>()

        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val currentCookie = iterator.next()
            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie)
            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie)
            }
        }

        persistor.removeAll(cookiesToRemove)

        return validCookies
    }

    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }

    @Synchronized
    override fun clearSession() {
        cache.clear()
        cache.addAll(persistor.loadAll())
    }

    @Synchronized
    override fun clear() {
        cache.clear()
        persistor.clear()
    }
}