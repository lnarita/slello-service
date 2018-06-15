package org.slello.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit


@Component
class JWT @Autowired constructor(@Value("\${jwt.secret}") val secret: String,
                                 @Value("\${jwt.expiration.duration}") val expiration: Long,
                                 @Value("\${jwt.expiration.timeunit}") val expirationTimeUnit: TimeUnit) {

    companion object {
        const val BEARER_AUTHORIZATION_HEADER_PREFIX = "Bearer "
    }

    val zoneOffset = ZoneOffset.UTC!!

    fun getUsernameFromToken(token: String): String {
        return getClaimFromToken(token, Claims::getSubject)
    }

    fun getIssuedAtDateFromToken(token: String): Date {
        return getClaimFromToken(token, Claims::getIssuedAt)
    }

    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Claims::getExpiration)
    }

    fun <T> getClaimFromToken(token: String, claimsResolver: Function1<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .body
    }

    private fun isTokenExpired(token: String): Boolean {
        val now = ZonedDateTime.now(zoneOffset).toInstant()
        val expiration = getExpirationDateFromToken(token)
        return now.isAfter(expiration.toInstant())
    }

    private fun ignoreTokenExpiration(token: String): Boolean {
        // here you specify tokens, for that the expiration is ignored
        return false
    }

    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Objects>()
        return buildToken(claims, userDetails.username)
    }

    private fun buildToken(claims: Map<String, Any>, subject: String): String {
        val createdDate = ZonedDateTime.now(zoneOffset).toInstant()
        val expirationDate = calculateExpirationDate(createdDate)

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(createdDate))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact()
    }

    fun canTokenBeRefreshed(token: String): Boolean {
        return (!isTokenExpired(token) || ignoreTokenExpiration(token))
    }

    fun refreshToken(token: String): String {
        val createdDate = ZonedDateTime.now(zoneOffset).toInstant()
        val expirationDate = calculateExpirationDate(createdDate)

        val claims = getAllClaimsFromToken(token)
        claims.issuedAt = Date.from(createdDate)
        claims.expiration = expirationDate

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact()
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = getUsernameFromToken(token)
        return (username == userDetails.username && !isTokenExpired(token))
    }

    private fun calculateExpirationDate(createdDate: Instant): Date {
        return Date.from(createdDate.plusMillis(expirationTimeUnit.toMillis(expiration)))
    }
}