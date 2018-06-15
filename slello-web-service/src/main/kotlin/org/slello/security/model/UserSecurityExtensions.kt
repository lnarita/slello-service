package org.slello.security.model

import org.slello.model.Authority
import org.slello.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

fun Authority.toGrantedAuthority(): GrantedAuthority = GrantedAuthority { name }

class ApplicationUserDetails(username: String, email: String, password: String, authority: Authority, enabled: Boolean) : User(username, email, password, authority, enabled), UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authority.reachableAuthorities.map { it.toGrantedAuthority() }.toMutableList()

    override fun isEnabled(): Boolean = enabled

    override fun getUsername(): String = id

    override fun isCredentialsNonExpired(): Boolean = enabled

    override fun getPassword(): String = secret

    override fun isAccountNonExpired(): Boolean = enabled

    override fun isAccountNonLocked(): Boolean = enabled

}