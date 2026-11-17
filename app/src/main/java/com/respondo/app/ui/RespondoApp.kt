package com.respondo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.respondo.ui.prospects.ProspectsScreen
import com.respondo.ui.prospects.ProspectViewModel

private data class Destination(val name: String, val symbol: String)

private val destinations = listOf(
    Destination("Dashboard", "⌂"),
    Destination("Prospects", "◎"),
    Destination("Conversations", "▤"),
    Destination("Campaigns", "↗"),
    Destination("Settings", "⚙"),
)

@Composable
fun RespondoApp() {
    var selected by rememberSaveable { mutableStateOf("Dashboard") }
    var openAddProspectOnEntry by rememberSaveable { mutableStateOf(false) }
    val prospectViewModel: ProspectViewModel = viewModel()
    val prospectState by prospectViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selected == destination.name,
                        onClick = {
                            selected = destination.name
                            openAddProspectOnEntry = false
                        },
                        icon = { Text(destination.symbol, fontSize = 20.sp) },
                        label = { Text(destination.name, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            BrandHeader()
            Spacer(Modifier.height(34.dp))
            when (selected) {
                "Dashboard" -> DashboardContent(prospectCount = prospectState.totalCount, onAddProspect = {
                    openAddProspectOnEntry = true
                    selected = "Prospects"
                })
                "Prospects" -> ProspectsScreen(
                    modifier = Modifier.weight(1f),
                    viewModel = prospectViewModel,
                    initiallyAdding = openAddProspectOnEntry,
                )
                else -> FeaturePlaceholder(selected)
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text("r", color = MaterialTheme.colorScheme.onPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("respondo", fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            Text("SALES WORKSPACE", fontSize = 9.sp, letterSpacing = 1.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("JD", modifier = Modifier.padding(11.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DashboardContent(prospectCount: Int, onAddProspect: () -> Unit) {
    Text("YOUR SALES WORKSPACE", fontSize = 11.sp, letterSpacing = 1.3.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(9.dp))
    Text("Welcome to Respondo", fontSize = 29.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
    Spacer(Modifier.height(7.dp))
    Text("Your sales workspace, at a glance.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    Spacer(Modifier.height(26.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(Modifier.padding(22.dp)) {
            Text("YOUR PIPELINE", color = Color(0xFF245846), fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("A clear view of what’s next.", color = Color(0xFF0B3225), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Add prospects to start building momentum.", color = Color(0xFF245846), fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Text("＋   Add a prospect", modifier = Modifier.clickable(onClick = onAddProspect), color = Color(0xFF0B3225), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }

    Spacer(Modifier.height(28.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Workspace", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text("OVERVIEW", fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("Prospects", prospectCount.toString(), if (prospectCount == 1) "In your workspace" else "Ready to connect", Modifier.weight(1f))
        MetricCard("Conversations", "0", "No activity yet", Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    MetricCard("Campaigns", "0", "Your next opportunity starts here", Modifier.fillMaxWidth())
    Spacer(Modifier.height(24.dp))
    Text("UP NEXT", fontSize = 10.sp, letterSpacing = 1.3.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Text("✦", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Your next step", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(if (prospectCount == 0) "Add your first prospect to get started." else "$prospectCount prospect${if (prospectCount == 1) "" else "s"} saved on this device.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeaturePlaceholder(name: String) {
    Text(name, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
    Text("Your $name workspace will appear here.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(24.dp))
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("A fresh start", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Respondo is ready when you are.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
