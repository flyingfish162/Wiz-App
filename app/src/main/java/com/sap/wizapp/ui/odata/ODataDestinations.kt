package com.sap.wizapp.ui.odata

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sap.cloud.mobile.kotlin.odata.EntityType
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty

const val NAV_ENTITY_LIST = "_entities_list"
const val NAV_ENTITY_NAV_LIST = "_navigate"


interface ODataNavigationCommand {
    val route: String
    val arguments: List<NamedNavArgument>
}

const val navigationPropertyNameArg = "navigation_property_name"

object EntitySetsDest : ODataNavigationCommand {
    override val arguments: List<NamedNavArgument>
        get() = listOf()
    override val route: String = "entity_sets"
}


class EntityNavigationCommands(private val entityType: EntityType) {

    val entityListNav = object : ODataNavigationCommand {
        override val arguments: List<NamedNavArgument>
            get() = listOf()
        override val route: String =  "${entityType.localName}/$NAV_ENTITY_LIST"
    }

    val toEntitiesNav = object : ODataNavigationCommand {
        override val arguments: List<NamedNavArgument>
            get() = listOf(
                navArgument(navigationPropertyNameArg) { type = NavType.StringType },
            )
        override val route: String = "${entityType.localName}/$NAV_ENTITY_NAV_LIST/{$navigationPropertyNameArg}/$NAV_ENTITY_LIST"
    }
}

fun NavHostController.navigateToEntityList(entityType: EntityType) {
    this.navigate("${entityType.localName}/$NAV_ENTITY_LIST")
}

fun NavHostController.navigateToNavigatePropertyList(
    navProp: NavigationProperty
) {
    this.navigate(
        "${navProp.relatedEntityType.localName}/$NAV_ENTITY_NAV_LIST/${navProp.name}/$NAV_ENTITY_LIST"
    )
}
