package org.slello.model

import arrow.core.getOrElse
import arrow.syntax.collections.firstOption

enum class Visibility(val id: Int) {
    OPEN(1),     // anyone can see, anyone can join
    CLOSED(2),   // anyone from the organization can see, anyone from the organization can join
    SECRET(3);   // private, invite only

    companion object {
        fun fromId(id: Int): Visibility = Visibility.values().filter { it.id == id }.firstOption().getOrElse { Visibility.OPEN }
    }
}