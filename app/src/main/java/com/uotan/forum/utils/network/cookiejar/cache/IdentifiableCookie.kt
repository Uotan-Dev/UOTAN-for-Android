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

package com.uotan.forum.utils.network.cookiejar.cache

import okhttp3.Cookie

/**
 * This class decorates a Cookie to re-implements equals() and hashcode() methods in order to identify
 * the cookie by the following attributes: name, domain, path, secure & hostOnly.<p>
 *
 * This new behaviour will be useful in determining when an already existing cookie in session must be overwritten.
 */
class IdentifiableCookie(private val cookie: Cookie) {

    fun getCookie(): Cookie = cookie

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdentifiableCookie

        return other.cookie.name == cookie.name &&
                other.cookie.domain == cookie.domain &&
                other.cookie.path == cookie.path &&
                other.cookie.secure == cookie.secure &&
                other.cookie.hostOnly == cookie.hostOnly
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + cookie.name.hashCode()
        result = 31 * result + cookie.domain.hashCode()
        result = 31 * result + cookie.path.hashCode()
        result = 31 * result + cookie.secure.hashCode()
        result = 31 * result + cookie.hostOnly.hashCode()
        return result
    }

    companion object {
        fun decorateAll(cookies: Collection<Cookie>): List<IdentifiableCookie> {
            return cookies.map { IdentifiableCookie(it) }
        }
    }
}