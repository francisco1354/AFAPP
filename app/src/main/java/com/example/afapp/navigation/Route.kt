package com.example.afapp.navigation


sealed class Route(val path: String) {

    companion object {
        const val FILTER_EMAIL_ARG = "filterEmail"
    }

    data object HomeBase : Route("home_base")

    data object HomeFiltered : Route("home_filtered?${FILTER_EMAIL_ARG}={${FILTER_EMAIL_ARG}}") {
        fun path(email: String) = "home_filtered?${FILTER_EMAIL_ARG}=${email}"
    }

    data object Login    : Route("login")
    data object Register : Route("register")

    data object Create   : Route("create")


    data object Detail   : Route("detail/{postId}") {
        fun path(id: String) = "detail/$id"
    }
    data object Profile : Route("profile")
}
