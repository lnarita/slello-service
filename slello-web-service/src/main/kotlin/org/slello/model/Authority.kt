package org.slello.model

import arrow.core.getOrElse
import arrow.syntax.collections.firstOption

enum class Authority(val id: Int, childrenAuthorities: List<Authority> = emptyList()) {
    UNKNOWN(0),
    EXT(1),
    USR(2, listOf(EXT)),
    ADM(3, listOf(EXT, USR));

    val reachableAuthorities: List<Authority> by lazy {
        childrenAuthorities + listOf(this)
    }

    companion object {
        fun fromId(id: Int): Authority = Authority.values().filter { it.id == id }.firstOption().getOrElse { Authority.UNKNOWN }
    }
}