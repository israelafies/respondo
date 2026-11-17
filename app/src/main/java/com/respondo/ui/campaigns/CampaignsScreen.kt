package com.respondo.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.respondo.data.campaigns.*
import java.text.DateFormat
import java.util.Date

private val campaignMuted = Color(0xFFA5B5AE)
private val campaignCard = Color(0xFF15201D)
private val campaignMint = Color(0xFF8CE0C0)

@Composable
fun CampaignsScreen(modifier: Modifier = Modifier, viewModel: CampaignViewModel) {
    val state by viewModel.uiState.collectAsState()
    var draft by remember { mutableStateOf<Campaign?>(null) }
    var selected by remember { mutableStateOf<Campaign?>(null) }
    var deleting by remember { mutableStateOf<Campaign?>(null) }
    if (draft != null) {
        CampaignForm(draft!!, state.offers, onCancel = { draft = null }, onSave = { viewModel.save(it) { draft = null } })
    } else if (selected != null) {
        val current = state.campaigns.firstOrNull { it.id == selected!!.id } ?: selected!!
        CampaignDetail(current, state.prospects, state.offers.firstOrNull { it.id == current.offerProfileId }?.offerName, viewModel, onBack = { selected = null }, onEdit = { draft = current; selected = null }, onDelete = { deleting = current; selected = null })
    } else {
        Column(modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) { Text("Campaigns", fontSize = 27.sp, fontWeight = FontWeight.SemiBold); Text("Plan and track each prospect's progress.", fontSize = 13.sp, color = campaignMuted) }
                Button(onClick = { draft = Campaign(name = "") }, shape = RoundedCornerShape(14.dp)) { Text("＋  Add") }
            }
            Spacer(Modifier.height(18.dp))
            if (state.campaigns.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 35.dp).background(campaignCard, RoundedCornerShape(22.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Build your first campaign", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.height(6.dp)); Text("Group prospects around an objective and track every opportunity.", color = campaignMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp)); Button(onClick = { draft = Campaign(name = "") }) { Text("＋  Create a campaign") }
                }
            } else LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                items(state.campaigns, key = { it.id }) { campaign ->
                    Card(onClick = { selected = campaign }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = campaignCard)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(campaign.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp); Spacer(Modifier.height(4.dp)); Text("${campaign.status.name.lowercase().replaceFirstChar { it.uppercase() }}  ·  ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(campaign.createdAt))}", color = campaignMuted, fontSize = 12.sp); if (campaign.objective.isNotBlank()) Text(campaign.objective, color = campaignMuted, fontSize = 12.sp, maxLines = 1) }
                            Text("›", color = campaignMuted, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
    deleting?.let { item -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete campaign?") }, text = { Text("${item.name} and its campaign memberships will be removed. Prospect records will remain.") }, confirmButton = { TextButton(onClick = { viewModel.delete(item) { deleting = null }; deleting = null }) { Text("Delete", color = Color(0xFFFF8A80)) } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }) }
}

@Composable
private fun CampaignForm(campaign: Campaign, offers: List<com.respondo.data.offers.OfferProfile>, onCancel: () -> Unit, onSave: (Campaign) -> Unit) {
    var name by remember(campaign.id) { mutableStateOf(campaign.name) }
    var objective by remember(campaign.id) { mutableStateOf(campaign.objective) }
    var description by remember(campaign.id) { mutableStateOf(campaign.description) }
    var offerId by remember(campaign.id) { mutableStateOf(campaign.offerProfileId) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onCancel) { Text("‹  Back") }; Text(if (campaign.id == 0L) "New campaign" else "Edit campaign", fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }
        Text("Set a clear objective and link an offer profile if useful.", fontSize = 13.sp, color = campaignMuted)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampaignField("Campaign name *", name, { name = it }, "e.g. Spring outreach")
            CampaignField("Objective", objective, { objective = it }, "What outcome are you aiming for?")
            CampaignField("Description", description, { description = it }, "Add campaign notes", 3)
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(offers.firstOrNull { it.id == offerId }?.offerName ?: "Link an offer profile (optional)") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("No offer profile") }, onClick = { offerId = null; expanded = false })
                    offers.forEach { offer -> DropdownMenuItem(text = { Text(offer.offerName) }, onClick = { offerId = offer.id; expanded = false }) }
                }
            }
            error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 4.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = { val message = CampaignValidation.error(name); error = message; if (message == null) onSave(campaign.copy(name = name.trim(), objective = objective.trim(), description = description.trim(), offerProfileId = offerId)) }, modifier = Modifier.weight(1f)) { Text("Save campaign") }
        }
    }
}

@Composable
private fun CampaignDetail(campaign: Campaign, prospects: List<com.respondo.data.prospects.Prospect>, offerName: String?, vm: CampaignViewModel, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val members by vm.members(campaign.id).collectAsState(initial = emptyList())
    val stats by vm.stats(campaign.id).collectAsState(initial = CampaignStats(0, 0, 0, 0, 0, 0))
    var addOpen by remember { mutableStateOf(false) }
    val assigned = members.map { it.prospectId }.toSet()
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("‹  Back") }; Column(Modifier.weight(1f)) { Text(campaign.name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold); Text("${campaign.status.name.lowercase().replaceFirstChar { it.uppercase() }} · Created ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(campaign.createdAt))}", color = campaignMuted, fontSize = 11.sp) }; TextButton(onClick = onEdit) { Text("Edit") } }
        if (campaign.objective.isNotBlank()) Text(campaign.objective, color = campaignMuted, fontSize = 13.sp)
        if (offerName != null) Text("Offer profile: $offerName", color = campaignMint, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        if (campaign.description.isNotBlank()) Text(campaign.description, color = campaignMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(top = 7.dp)) {
            TextButton(onClick = { vm.save(campaign.copy(status = if (campaign.status == CampaignStatus.PAUSED) CampaignStatus.ACTIVE else CampaignStatus.PAUSED)) {} }) { Text(if (campaign.status == CampaignStatus.PAUSED) "Resume" else "Pause") }
            if (campaign.status != CampaignStatus.COMPLETED) TextButton(onClick = { vm.save(campaign.copy(status = CampaignStatus.COMPLETED)) {} }) { Text("Complete") }
            TextButton(onClick = onDelete) { Text("Delete", color = Color(0xFFFF8A80)) }
        }
        Spacer(Modifier.height(8.dp))
        Text("CAMPAIGN DASHBOARD", fontSize = 10.sp, letterSpacing = 1.2.sp, color = campaignMuted, fontWeight = FontWeight.Bold)
        Text("${stats.total} prospects   ·   ${stats.contacted} contacted   ·   ${stats.interested} interested", fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        Text("${stats.negotiating} negotiating   ·   ${stats.converted} conversions   ·   ${stats.lost} lost", fontSize = 12.sp, color = campaignMuted, modifier = Modifier.padding(top = 3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) { Text("Prospects", fontSize = 17.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); Button(onClick = { addOpen = true }) { Text("＋ Add") } }
        if (members.isEmpty()) Text("Add existing prospects to track their progress here.", color = campaignMuted, fontSize = 13.sp)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members, key = { it.prospectId }) { member ->
                val prospect = prospects.firstOrNull { it.id == member.prospectId }
                if (prospect != null) ProspectStageCard(prospect.fullName, prospect.businessName, member.stage, onStage = { vm.updateStage(campaign.id, member.prospectId, it) }, onRemove = { vm.removeProspect(campaign.id, member.prospectId) })
            }
        }
    }
    if (addOpen) AlertDialog(onDismissRequest = { addOpen = false }, title = { Text("Add a prospect") }, text = {
        if (prospects.none { it.id !in assigned }) Text("All prospects are already assigned to this campaign.")
        else Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) { prospects.filter { it.id !in assigned }.forEach { prospect -> TextButton(onClick = { vm.addProspect(campaign.id, prospect.id); addOpen = false }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth()) { Text(prospect.fullName, fontWeight = FontWeight.Medium); Text(prospect.businessName, color = campaignMuted, fontSize = 12.sp) } } } }
    }, confirmButton = { TextButton(onClick = { addOpen = false }) { Text("Done") } })
}

@Composable
private fun ProspectStageCard(name: String, business: String, stage: SalesStage, onStage: (SalesStage) -> Unit, onRemove: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = campaignCard)) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold); Text(business, color = campaignMuted, fontSize = 12.sp) }; TextButton(onClick = onRemove) { Text("Remove", color = Color(0xFFFF8A80), fontSize = 11.sp) } }
            Box {
                Text("Stage: ${stage.label()}  ▾", color = campaignMint, fontSize = 12.sp, modifier = Modifier.clickable { expanded = true }.padding(top = 4.dp))
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { SalesStage.entries.forEach { item -> DropdownMenuItem(text = { Text(item.label()) }, onClick = { onStage(item); expanded = false }) } }
            }
        }
    }
}

private fun SalesStage.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@Composable
private fun CampaignField(label: String, value: String, change: (String) -> Unit, placeholder: String, lines: Int = 1) {
    Column { Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp)); OutlinedTextField(value, change, modifier = Modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, minLines = lines, shape = RoundedCornerShape(14.dp)) }
}
