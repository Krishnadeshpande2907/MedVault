package com.medvault.ui.navigation

/**
 * Sealed class defining all navigation routes in the app.
 */
sealed class Screen(val route: String) {
    data object Timeline : Screen("timeline")
    data object Settings : Screen("settings")
    data object AddEditVisit : Screen("add_edit_visit?visitId={visitId}") {
        fun createRoute(visitId: String? = null): String =
            if (visitId != null) "add_edit_visit?visitId=$visitId"
            else "add_edit_visit"
    }
    data object VisitDetail : Screen("visit_detail/{visitId}") {
        fun createRoute(visitId: String): String = "visit_detail/$visitId"
    }
    data object MediaViewer : Screen("media_viewer/{visitId}?startIndex={startIndex}") {
        fun createRoute(visitId: String, startIndex: Int = 0): String =
            "media_viewer/$visitId?startIndex=$startIndex"
    }
    data object Export : Screen("export/{visitId}") {
        fun createRoute(visitId: String): String = "export/$visitId"
    }
    data object Lock : Screen("lock")
}
