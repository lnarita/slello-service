package org.slello.v1.rest.validator

import arrow.Kind
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.applicativeError
import arrow.data.*
import arrow.typeclasses.ApplicativeError
import org.slello.v1.rest.model.request.LoginRequest
import org.slello.v1.rest.model.response.ResponseError
import org.springframework.util.Base64Utils

sealed class Rules<F>(A: ApplicativeError<F, Nel<ResponseError>>) : ApplicativeError<F, Nel<ResponseError>> by A {
    fun LoginRequest.hasCredentials(headerPrefix: String): Kind<F, LoginRequest> {
        if (authenticationHeader != null && authenticationHeader.startsWith(headerPrefix)) {
            return try {
                val decodedAuthentication = String(Base64Utils.decodeFromString(authenticationHeader.removePrefix(headerPrefix))).split(":")

                if (decodedAuthentication.size == 2) {
                    just(this.copy(username = decodedAuthentication[0], password = decodedAuthentication[1]))
                } else {
                    raiseError(ResponseError.InvalidAuthenticationHeader(authenticationHeader).nel())
                }
            } catch (e: IllegalArgumentException) {
                raiseError(ResponseError.InvalidAuthenticationHeader(authenticationHeader).nel())
            }
        } else {
            return raiseError(ResponseError.Unauthorized().nel())
        }
    }


    object ErrorAccumulationStrategy :
            Rules<ValidatedPartialOf<Nel<ResponseError>>>(Validated.applicativeError(NonEmptyList.semigroup()))

    object FailFastStrategy :
            Rules<EitherPartialOf<Nel<ResponseError>>>(Either.applicativeError())

    companion object {
        infix fun <A> failFast(f: FailFastStrategy.() -> A): A = f(FailFastStrategy)
        infix fun <A> accumulateErrors(f: ErrorAccumulationStrategy.() -> A): A = f(ErrorAccumulationStrategy)
    }

}