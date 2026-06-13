package app.revanced.manager.ui.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.revanced.manager.R
import app.revanced.manager.network.dto.ReVancedAnnouncement
import app.revanced.manager.ui.component.AlertDialogExtended
import app.revanced.manager.ui.component.AvailableUpdateDialog
import app.revanced.manager.ui.component.ConfirmDialog
import app.revanced.manager.ui.component.NotificationCard
import app.revanced.manager.ui.component.NotificationCardType
import app.revanced.manager.ui.component.PillTab
import app.revanced.manager.ui.component.PillTabBar
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.ui.component.haptics.HapticExtendedFloatingActionButton
import app.revanced.manager.ui.component.sources.ImportSourceDialog
import app.revanced.manager.ui.component.sources.ImportSourceDialogStrings
import app.revanced.manager.ui.model.SelectedApp
import app.revanced.manager.ui.model.navigation.SelectedApplicationInfo
import app.revanced.manager.ui.viewmodel.DashboardViewModel
import app.revanced.manager.ui.viewmodel.PatchesSelectorViewModel
import app.revanced.manager.util.RequestInstallAppsContract
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

enum class DashboardPage(
    val titleResId: Int,
    val icon: ImageVector
) {
    DASHBOARD(R.string.tab_apps, Icons.Outlined.Apps),
    BUNDLES(R.string.tab_patches, Icons.Outlined.Source),
}

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    vm: DashboardViewModel = koinViewModel(),
    onSettingsClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onAnnouncementClick: (ReVancedAnnouncement) -> Unit,
    onAppClick: (String) -> Unit,
    onPatchableAppClick: (String) -> Unit,
    onStorageSelect: (SelectedApp.Local) -> Unit,
    onBundleClick: (Int) -> Unit
) {
    val bundleDownloadError by vm.bundleDownloadError.collectAsStateWithLifecycle(null)
    val sourcesNotDownloaded by vm.sourcesNotDownloaded.collectAsStateWithLifecycle(false)
    val sourceUpdatesAvailable by vm.sourceUpdatesAvailable.collectAsStateWithLifecycle(false)
    val managerAutoUpdates by vm.prefs.managerAutoUpdates.getAsState()
    val showManagerUpdateDialogOnLaunch by vm.prefs.showManagerUpdateDialogOnLaunch.getAsState()
    val disablePatchVersionCompatCheck by vm.prefs.disablePatchVersionCompatCheck.getAsState()
    val disableSelectionWarning by vm.prefs.disableSelectionWarning.getAsState()
    val disableUniversalPatchCheck by vm.prefs.disableUniversalPatchCheck.getAsState()
    val suggestedVersionSafeguard by vm.prefs.suggestedVersionSafeguard.getAsState()
    val safeguardsToggled by remember(
        disablePatchVersionCompatCheck,
        disableSelectionWarning,
        disableUniversalPatchCheck,
        suggestedVersionSafeguard
    ) {
        derivedStateOf {
            disablePatchVersionCompatCheck ||
                    disableSelectionWarning ||
                    disableUniversalPatchCheck ||
                    !suggestedVersionSafeguard
        }
    }
    val hasUpdate by vm.hasUpdate.collectAsStateWithLifecycle()
    val updateVersion by vm.updateVersion.collectAsStateWithLifecycle()
    val androidContext = LocalContext.current
    val resources = LocalResources.current
    val logoPainter = rememberDrawablePainter(drawable = remember(resources) {
        AppCompatResources.getDrawable(androidContext, R.drawable.ic_logo_ring)
    })
    val composableScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = DashboardPage.DASHBOARD.ordinal,
        initialPageOffsetFraction = 0f
    ) { DashboardPage.entries.size }

    val appsLazyListState = rememberLazyListState()
    val appsSearchLazyListState = rememberLazyListState()
    var appsSearchExpanded by rememberSaveable { mutableStateOf(false) }

    val dashboardPatchesParams = remember {
        SelectedApplicationInfo.PatchesSelector.ViewModelParams(
            app = SelectedApp.Search("", null),
            currentSelection = null,
            options = emptyMap(),
            readOnly = true,
            browseAllBundles = true
        )
    }
    val dashboardPatchesViewModel = koinViewModel<PatchesSelectorViewModel>(key = "dashboard-patches") {
        parametersOf(dashboardPatchesParams)
    }
    val dashboardPatchesBundles by dashboardPatchesViewModel.bundlesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var patchesSourceEditMode by rememberSaveable { mutableStateOf(false) }
    var sourceDeleteUid by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != DashboardPage.BUNDLES.ordinal) {
            patchesSourceEditMode = false
        }
    }

    sourceDeleteUid?.let { uid ->
        val sourceName = dashboardPatchesBundles.firstOrNull { it.uid == uid }?.name
            ?: return@let
        ConfirmDialog(
            onDismiss = { sourceDeleteUid = null },
            onConfirm = { vm.deleteSource(uid) },
            title = stringResource(R.string.delete),
            description = stringResource(R.string.patches_delete_single_dialog_description, sourceName),
            icon = Icons.Outlined.Delete
        )
    }

    var showAddBundleDialog by rememberSaveable { mutableStateOf(false) }
    if (showAddBundleDialog) {
        ImportSourceDialog(
            strings = ImportSourceDialogStrings.PATCHES,
            onDismiss = { showAddBundleDialog = false },
            onLocalSubmit = { patches ->
                showAddBundleDialog = false
                patchesSourceEditMode = false
                vm.createLocalSource(patches)
            },
            onRemoteSubmit = { url, autoUpdate ->
                showAddBundleDialog = false
                patchesSourceEditMode = false
                vm.createRemoteSource(url, autoUpdate)
            }
        )
    }

    var showUpdateDialog by rememberSaveable { mutableStateOf(true) }
    if (managerAutoUpdates && showUpdateDialog && showManagerUpdateDialogOnLaunch && hasUpdate) {
        AvailableUpdateDialog(
            onDismiss = { showUpdateDialog = false },
            setShowManagerUpdateDialogOnLaunch = vm::setShowManagerUpdateDialogOnLaunch,
            onConfirm = onUpdateClick,
            newVersion = updateVersion!!
        )
    }

    var pendingPatchablePackage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingStorageSelection by rememberSaveable { mutableStateOf<SelectedApp.Local?>(null) }

    fun clearPendingSelection() {
        pendingPatchablePackage = null
        pendingStorageSelection = null
    }

    fun resumePendingSelection() {
        pendingPatchablePackage?.let {
            clearPendingSelection()
            onPatchableAppClick(it)
            return
        }

        pendingStorageSelection?.let {
            clearPendingSelection()
            onStorageSelect(it)
        }
    }

    var showAndroid11Dialog by rememberSaveable { mutableStateOf(false) }
    val installAppsPermissionLauncher =
        rememberLauncherForActivityResult(RequestInstallAppsContract) { granted ->
            showAndroid11Dialog = false
            if (granted) {
                resumePendingSelection()
            } else {
                clearPendingSelection()
            }
        }

    if (showAndroid11Dialog) {
        Android11Dialog(
            onDismissRequest = {
                showAndroid11Dialog = false
                clearPendingSelection()
            },
            onContinue = {
                installAppsPermissionLauncher.launch(androidContext.packageName)
            }
        )
    }

    fun onPatchableSelection(packageName: String) {
        if (vm.android11BugActive) {
            clearPendingSelection()
            pendingPatchablePackage = packageName
            showAndroid11Dialog = true
            return
        }

        onPatchableAppClick(packageName)
    }

    fun onStorageSelection(app: SelectedApp.Local) {
        if (vm.android11BugActive) {
            clearPendingSelection()
            pendingStorageSelection = app
            showAndroid11Dialog = true
            return
        }

        onStorageSelect(app)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        val isTv = remember {
            androidContext.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (isTv) {
                TvNavigationRail(
                    pagerState = pagerState,
                    hasUpdate = hasUpdate,
                    unreadAnnouncement = vm.unreadAnnouncement != null,
                    onSettingsClick = onSettingsClick,
                    onAnnouncementsClick = onAnnouncementsClick,
                    onUpdateClick = onUpdateClick,
                    logoPainter = logoPainter,
                    composableScope = composableScope
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (!isTv) {
                    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(statusBarHeight + 96.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                if (!isTv && navBarHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(navBarHeight)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    )
                }

                Scaffold(
                topBar = {
                    if (!isTv) {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = logoPainter,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(stringResource(R.string.app_name))
                                }
                            },
                            actions = {
                                if (hasUpdate) {
                                    TooltipIconButton(
                                        onClick = onUpdateClick,
                                        tooltip = stringResource(R.string.update),
                                    ) { contentDescription ->
                                        BadgedBox(badge = { Badge(modifier = Modifier.size(6.dp)) }) {
                                            Icon(Icons.Filled.Update, contentDescription)
                                        }
                                    }
                                }
                                TooltipIconButton(
                                    onClick = onAnnouncementsClick,
                                    tooltip = stringResource(R.string.announcements),
                                ) { contentDescription ->
                                    BadgedBox(
                                        badge = {
                                            if (vm.unreadAnnouncement != null) {
                                                Badge(modifier = Modifier.size(6.dp))
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Notifications,
                                            contentDescription
                                        )
                                    }
                                }
                                TooltipIconButton(
                                    onClick = onSettingsClick,
                                    tooltip = stringResource(R.string.settings),
                                ) { contentDescription ->
                                    BadgedBox(
                                        badge = {
                                            if (safeguardsToggled) {
                                                Badge(
                                                    modifier = Modifier.size(6.dp),
                                                    containerColor = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Settings, contentDescription)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                },
                containerColor = Color.Transparent,
                floatingActionButton = {
                    val currentScrollState =
                        if (appsSearchExpanded) appsSearchLazyListState else appsLazyListState
                    val showBackToTop by remember(currentScrollState) {
                        derivedStateOf { currentScrollState.firstVisibleItemIndex > 0 }
                    }

                    DashboardFab(
                        pagerState = pagerState,
                        patchesSourceEditMode = patchesSourceEditMode,
                        onEnablePatchesSourceEditMode = { patchesSourceEditMode = true },
                        onAddBundleClick = {
                            showAddBundleDialog = true
                        },
                        showScrollToTop = showBackToTop && !isTv,
                        onScrollToTop = {
                            composableScope.launch {
                                currentScrollState.animateScrollToItem(0)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(Modifier.padding(paddingValues)) {
                    if (!isTv) {
                        PillTabBar(
                            pagerState = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            DashboardPage.entries.forEachIndexed { index, page ->
                                PillTab(
                                    index = index,
                                    onClick = { composableScope.launch { pagerState.animateScrollToPage(index) } },
                                    text = { Text(stringResource(page.titleResId)) },
                                    icon = { Icon(page.icon, null) }
                                )
                            }
                        }
                    }

                    Notifications(
                        if (bundleDownloadError != null) {
                            {
                                NotificationCard(
                                    type = NotificationCardType.ERROR,
                                    icon = Icons.Outlined.WarningAmber,
                                    title = stringResource(R.string.api_not_working_title),
                                    text = stringResource(R.string.api_not_working_description),
                                    onClick = onSettingsClick
                                )
                            }
                        } else null,
                        if (sourceUpdatesAvailable) {
                            {
                                NotificationCard(
                                    type = NotificationCardType.WARNING,
                                    icon = Icons.Outlined.Refresh,
                                    text = stringResource(R.string.banner_sources_not_updated_description),
                                    onClick = vm::downloadSources
                                )
                            }
                        } else if (sourcesNotDownloaded && bundleDownloadError == null) {
                            {
                                NotificationCard(
                                    type = NotificationCardType.WARNING,
                                    icon = Icons.Outlined.Refresh,
                                    text = stringResource(R.string.banner_sources_not_downloaded_description),
                                    onClick = vm::downloadSources
                                )
                            }
                        } else null,
                        vm.unreadAnnouncement?.let { announcement ->
                            {
                                NotificationCard(
                                    title = stringResource(R.string.new_announcement),
                                    text = announcement.title,
                                    icon = Icons.Filled.Notifications,
                                    type = if (announcement.level > 0) NotificationCardType.ERROR else NotificationCardType.NORMAL,
                                    onClick = {
                                        vm.markUnreadAnnouncementRead()
                                        onAnnouncementClick(announcement)
                                    },
                                    onDismiss = vm::markUnreadAnnouncementRead
                                )
                            }
                        }
                    )

                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !isTv,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        when (DashboardPage.entries[index]) {
                            DashboardPage.DASHBOARD -> {
                                AppsScreen(
                                    onAppClick = { onAppClick(it.currentPackageName) },
                                    onPatchableAppClick = ::onPatchableSelection,
                                    onStorageSelect = { selectedApp -> onStorageSelection(selectedApp)},
                                    lazyListState = appsLazyListState,
                                    searchLazyListState = appsSearchLazyListState,
                                    onSearchExpandedChange = { appsSearchExpanded = it }
                                )
                            }

                            DashboardPage.BUNDLES -> {
                                BackHandler {
                                    if (patchesSourceEditMode) {
                                        patchesSourceEditMode = false
                                        return@BackHandler
                                    }
                                    composableScope.launch {
                                        pagerState.animateScrollToPage(DashboardPage.DASHBOARD.ordinal)
                                    }
                                }

                                PatchesSelectorScreen(
                                    onSave = { _, _ -> },
                                    onBackClick = {
                                        if (patchesSourceEditMode) {
                                            patchesSourceEditMode = false
                                            return@PatchesSelectorScreen
                                        }
                                        composableScope.launch {
                                            pagerState.animateScrollToPage(DashboardPage.DASHBOARD.ordinal)
                                        }
                                    },
                                    onBundleInfoClick = onBundleClick,
                                    isSourceEditMode = patchesSourceEditMode,
                                    onSourceDeleteRequest = { sourceDeleteUid = it },
                                    viewModel = dashboardPatchesViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardFab(
    pagerState: PagerState,
    patchesSourceEditMode: Boolean,
    onEnablePatchesSourceEditMode: () -> Unit,
    onAddBundleClick: () -> Unit,
    showScrollToTop: Boolean,
    onScrollToTop: () -> Unit
) {
    val fabState = when (pagerState.currentPage) {
        DashboardPage.DASHBOARD.ordinal -> {
            if (showScrollToTop) DashboardFabState.ScrollToTop else DashboardFabState.Hidden
        }

        DashboardPage.BUNDLES.ordinal -> {
            if (patchesSourceEditMode) DashboardFabState.AddBundles else DashboardFabState.EditBundles
        }

        else -> DashboardFabState.Hidden
    }

    AnimatedVisibility(
        visible = fabState != DashboardFabState.Hidden, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()
    ) {
        HapticExtendedFloatingActionButton(
            onClick = {
                when (fabState) {
                    DashboardFabState.AddBundles -> onAddBundleClick()
                    DashboardFabState.EditBundles -> onEnablePatchesSourceEditMode()
                    DashboardFabState.ScrollToTop -> onScrollToTop()
                    else -> {}
                }
            },
            tooltip = stringResource(
                if (fabState == DashboardFabState.AddBundles) R.string.fab_add_patches else R.string.edit
            ),
            expanded = fabState == DashboardFabState.AddBundles,
            icon = {
                AnimatedContent(
                    targetState = fabState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 60)) +
                                scaleIn(
                                    animationSpec = tween(durationMillis = 180, delayMillis = 60),
                                    initialScale = 0.85f
                                )) togetherWith
                                (fadeOut(animationSpec = tween(durationMillis = 90)) +
                                        scaleOut(
                                            animationSpec = tween(durationMillis = 90),
                                            targetScale = 0.85f
                                        ))
                    },
                    label = "dashboard_fab_icon_transition"
                ) { state ->
                    when (state) {
                        DashboardFabState.EditBundles -> {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }

                        DashboardFabState.AddBundles -> {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }

                        DashboardFabState.ScrollToTop -> {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                        }

                        else -> {}
                    }
                }
            },
            text = { Text(stringResource(R.string.fab_add_patches)) }
        )
    }
}

private enum class DashboardFabState {
    Hidden,
    EditBundles,
    AddBundles,
    ScrollToTop
}

@Composable
fun Notifications(
    vararg notifications: (@Composable () -> Unit)?,
) {
    val activeNotifications = notifications.filterNotNull()

    if (activeNotifications.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            activeNotifications.forEach { notification ->
                notification()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Android11Dialog(onDismissRequest: () -> Unit, onContinue: () -> Unit) {
    AlertDialogExtended(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onContinue, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.continue_))
            }
        },
        title = {
            Text(stringResource(R.string.android_11_bug_dialog_title))
        },
        icon = {
            Icon(Icons.Outlined.BugReport, null)
        },
        text = {
            Text(stringResource(R.string.android_11_bug_dialog_description))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TvNavigationRail(
    pagerState: PagerState,
    hasUpdate: Boolean,
    unreadAnnouncement: Boolean,
    onSettingsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onUpdateClick: () -> Unit,
    logoPainter: androidx.compose.ui.graphics.painter.Painter,
    composableScope: kotlinx.coroutines.CoroutineScope
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo at top
        Image(
            painter = logoPainter,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs (Apps, Patches)
        DashboardPage.entries.forEachIndexed { index, page ->
            val isSelected = pagerState.currentPage == index
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            val backgroundColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isFocused -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                else -> Color.Transparent
            }
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                              else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            composableScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = stringResource(page.titleResId),
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Announcements Tab (if TV, show on rail)
        val annInteractionSource = remember { MutableInteractionSource() }
        val annFocused by annInteractionSource.collectIsFocusedAsState()
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (annFocused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else Color.Transparent)
                .clickable(
                    interactionSource = annInteractionSource,
                    indication = null,
                    onClick = onAnnouncementsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (unreadAnnouncement) {
                        Badge(modifier = Modifier.size(6.dp))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.announcements),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Update button (only visible on TV if update is available)
        if (hasUpdate) {
            val updateInteractionSource = remember { MutableInteractionSource() }
            val updateFocused by updateInteractionSource.collectIsFocusedAsState()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (updateFocused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) 
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                    .clickable(
                        interactionSource = updateInteractionSource,
                        indication = null,
                        onClick = onUpdateClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(badge = { Badge(modifier = Modifier.size(6.dp)) }) {
                    Icon(
                        imageVector = Icons.Filled.Update,
                        contentDescription = stringResource(R.string.update),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings at bottom
        val settingsInteractionSource = remember { MutableInteractionSource() }
        val settingsFocused by settingsInteractionSource.collectIsFocusedAsState()
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (settingsFocused) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else Color.Transparent)
                .clickable(
                    interactionSource = settingsInteractionSource,
                    indication = null,
                    onClick = onSettingsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
