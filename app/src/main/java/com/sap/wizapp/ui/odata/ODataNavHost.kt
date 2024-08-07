package com.sap.wizapp.ui.odata

import android.app.Application
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata
import com.sap.cloud.android.odata.espmcontainer.ProductCategory
import com.sap.wizapp.ui.odata.screens.*
import com.sap.wizapp.ui.odata.viewmodel.*
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntitiesScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntityEditScreen

const val SETTINGS_SCREEN_ROUTE = "settings"

@Composable
fun ODataNavHost(
    navController: NavHostController,
    windowSize: WindowSizeClass,
    modifier: Modifier
) {
    val isExpandedScreen = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
    NavHost(
        navController = navController,
        startDestination = EntityNavigationCommands(ESPMContainerMetadata.EntityTypes.productCategory).entityListNav.route,
        modifier = modifier
    ) {
        composable(route = EntityNavigationCommands(ESPMContainerMetadata.EntityTypes.productCategory).entityListNav.route) {
            val viewModel: ODataViewModel = viewModel(
                factory = ODataEntityViewModelFactory(
                    LocalContext.current.applicationContext as Application,
                    ESPMContainerMetadata.EntityTypes.productCategory,
                    ESPMContainerMetadata.EntitySets.productCategories,
                    getOrderByProperty(ESPMContainerMetadata.EntityTypes.productCategory),
                )
            )

            ODataScreen(
                navController,
                isExpandedScreen,
                viewModel,
                ProductCategoryEntitiesExpandScreen,
                ProductCategoryEntitiesScreen,
                ProductCategoryEntityEditScreen,
                ProductCategoryEntityDetailScreen
            )
        }

        composable(route = EntitySetsDest.route) {
            EntitySetScreen(
                getEntitySetScreenInfoList(),
                navController::navigateToEntityList,
            ) { navController.navigate(SETTINGS_SCREEN_ROUTE) }
        }

        composable(route = SETTINGS_SCREEN_ROUTE) {
            SettingsScreen(navigateUp = navController::navigateUp)
        }

        //EntitySets
        EntityScreenInfo.entries.forEach {screenInfo ->
            val entityType = screenInfo.entityType
            val entitySet = screenInfo.entitySet
            val entityExpandScreen = screenInfo.entityExpandScreen
            val entityListScreen = screenInfo.entityListScreen
            val entityEditScreen = screenInfo.entityEditScreen
            val entityDetailScreen = screenInfo.entityDetailScreen
            navigation(
                startDestination = EntityNavigationCommands(entityType).entityListNav.route,
                route = entityType.name
            ) {
                composable(route = EntityNavigationCommands(entityType).entityListNav.route) {
                    val viewModel: ODataViewModel = viewModel(
                        factory = ODataEntityViewModelFactory(
                            LocalContext.current.applicationContext as Application,
                            entityType,
                            entitySet,
                            getOrderByProperty(entityType),
                            if (entityType == ESPMContainerMetadata.EntityTypes.product)
                                navController.previousBackStackEntry?.savedStateHandle?.get<ProductCategory>(
                                    "productCategory"
                                )
                            else null
                        )
                    )

                    ODataScreen(
                        navController,
                        isExpandedScreen,
                        viewModel,
                        entityExpandScreen,
                        entityListScreen,
                        entityEditScreen,
                        entityDetailScreen
                    )
                }

                composable(
                    route = EntityNavigationCommands(entityType).toEntitiesNav.route,
                    arguments = EntityNavigationCommands(entityType).toEntitiesNav.arguments
                ) { navBackStackEntry ->
                    val parent =
                        navController.previousBackStackEntry?.savedStateHandle?.get<EntityValue>(
                            "master"
                        )
                    val navProperty =
                        navBackStackEntry.arguments?.getString(navigationPropertyNameArg)

                    ODataScreen(
                        navController,
                        isExpandedScreen,
                        viewModel(
                            factory = ODataEntityViewModelFactory(
                                LocalContext.current.applicationContext as Application,
                                entityType,
                                entitySet,
                                getOrderByProperty(entityType),
                                parent,
                                navProperty,
                            )
                        ),
                        entityExpandScreen,
                        entityListScreen,
                        entityEditScreen,
                        entityDetailScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ODataScreen(
    navController: NavHostController, isExpandedScreen: Boolean, viewModel: ODataViewModel,
    entityExpandScreen: @Composable (
        navigateToHome: () -> Unit,
        navigateUp: () -> Unit,
        onNavigateProperty: (EntityValue, NavigationProperty) -> Unit,
        viewModel: ODataViewModel,
    ) -> Unit,
    entityListScreen: @Composable (
        navigateToHome: () -> Unit, navigateUp: () -> Unit, viewModel: ODataViewModel, isExpandedScreen: Boolean
    ) -> Unit,
    entityEditScreen: @Composable (
        navigateUp: () -> Unit, viewModel: ODataViewModel, isExpandedScreen: Boolean
    ) -> Unit,
    entityDetailScreen: @Composable (
        onNavigateProperty: (EntityValue, NavigationProperty) -> Unit, navigateUp: () -> Unit, viewModel: ODataViewModel, isExpandedScreen: Boolean
    ) -> Unit,
) {
    val onNavigateProperty: (EntityValue, NavigationProperty) -> Unit = { master, navProp ->
        navController.currentBackStackEntry?.savedStateHandle?.set(
            key = "master", value = master
        )
        navController.navigateToNavigatePropertyList(navProp)
    }

    if (isExpandedScreen) {
        entityExpandScreen(
            navigateToHome = {},
            navigateUp = navController::navigateUp,
            onNavigateProperty = onNavigateProperty,
            viewModel = viewModel
        )
    } else {
        val uiState by viewModel.odataUIState.collectAsState()
        if (!uiState.isEntityFocused) {
            entityListScreen(
                navigateToHome = {
                    if (viewModel.entityType != ESPMContainerMetadata.EntityTypes.productCategory) {
                        navController.popBackStack(
                            EntityNavigationCommands(ESPMContainerMetadata.EntityTypes.productCategory).entityListNav.route,false
                        )
                    }
                },
                navigateUp = {
                    if (viewModel.entityType == ESPMContainerMetadata.EntityTypes.productCategory) {
                        navController.navigate(EntitySetsDest.route)
                    } else {
                        navController.navigateUp()
                    }
                },
                viewModel = viewModel,
                isExpandedScreen = false
            )
        } else {
            when (uiState.entityOperationType) {
                EntityOperationType.DETAIL -> if (viewModel.entityType == ESPMContainerMetadata.EntityTypes.productCategory) {
                    viewModel.lostEntityFocus()
                    val productCategory = uiState.masterEntity as ProductCategory
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        key = "productCategory", value = productCategory
                    )
                    navController.navigate(EntityNavigationCommands(ESPMContainerMetadata.EntityTypes.product).entityListNav.route)
                } else{
                    entityDetailScreen(
                        onNavigateProperty = onNavigateProperty,
                        navigateUp = viewModel::lostEntityFocus,
                        viewModel = viewModel,
                        isExpandedScreen = false
                    )
                }

                EntityOperationType.CREATE, EntityUpdateOperationType.UPDATE_FROM_LIST, EntityUpdateOperationType.UPDATE_FROM_DETAIL -> entityEditScreen(
                    navigateUp = if (uiState.entityOperationType != EntityOperationType.CREATE) viewModel::exitUpdate else viewModel::exitCreation,
                    viewModel = viewModel,
                    isExpandedScreen = false
                )

                EntityOperationType.UNSPECIFIED -> {}
            }
        }
    }
}
