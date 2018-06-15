package org.slello.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
open class User(@Id val id: String, val email: String, val secret: String, val authority: Authority, val enabled: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false
        if (email != other.email) return false
        if (secret != other.secret) return false
        if (authority != other.authority) return false
        if (enabled != other.enabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + authority.hashCode()
        result = 31 * result + enabled.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(id='$id', email='$email', secret='[PROTECTED]', authority=$authority, enabled=$enabled)"
    }

}