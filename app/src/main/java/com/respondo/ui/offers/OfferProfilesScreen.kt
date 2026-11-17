package com.respondo.ui.offers

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.respondo.data.offers.OfferProfile

private val muted = Color(0xFFA5B5AE)
private val card = Color(0xFF15201D)
private val mint = Color(0xFF8CE0C0)

@Composable
fun OfferProfilesScreen(modifier: Modifier = Modifier, viewModel: OfferProfileViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editing by remember { mutableStateOf<OfferProfile?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<OfferProfile?>(null) }
    var deleting by remember { mutableStateOf<OfferProfile?>(null) }

    when {
        adding || editing != null -> OfferProfileForm(
            profile = editing,
            isSaving = state.isSaving,
            error = state.error,
            onSave = { viewModel.save(it) { adding = false; editing = null } },
            onCancel = { adding = false; editing = null; viewModel.clearError() },
        )
        else -> Column(modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Offer profiles", fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Text("Keep your services and sales context in one place.", fontSize = 13.sp, color = muted)
                }
                Button(onClick = { adding = true; viewModel.clearError() }, shape = RoundedCornerShape(14.dp)) { Text("＋  Add") }
            }
            Spacer(Modifier.height(18.dp))
            state.error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }
            if (state.profiles.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 35.dp).clip(RoundedCornerShape(22.dp)).background(card).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF202C28)), contentAlignment = Alignment.Center) { Text("✦", fontSize = 25.sp, color = mint) }
                    Spacer(Modifier.height(14.dp))
                    Text("Describe what you offer", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Save a profile for each service or product you sell.", color = muted, fontSize = 13.sp)
                    Spacer(Modifier.height(17.dp))
                    Button(onClick = { adding = true }) { Text("＋  Create an offer profile") }
                }
            } else {
                Text("${state.profiles.size} ${if (state.profiles.size == 1) "PROFILE" else "PROFILES"}", fontSize = 10.sp, letterSpacing = 1.3.sp, color = muted)
                Spacer(Modifier.height(9.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxSize()) {
                    items(state.profiles, key = { it.id }) { profile ->
                        Card(onClick = { selected = profile }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF202C28)), contentAlignment = Alignment.Center) { Text(profile.offerName.take(1).uppercase(), color = mint, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.width(13.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(profile.offerName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                        if (profile.isActive) Text("  ACTIVE", color = mint, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(profile.providerName, color = muted, fontSize = 12.sp)
                                    if (profile.description.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(profile.description, color = muted, fontSize = 12.sp, maxLines = 2) }
                                }
                                Text("›", color = muted, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { profile ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Column {
                Text(profile.offerName, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(profile.providerName, color = muted, fontSize = 14.sp)
                if (profile.isActive) Text("ACTIVE OFFER", color = mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                OfferDetail("Description", profile.description)
                OfferDetail("Target customers", profile.targetCustomers)
                OfferDetail("Problems solved", profile.problemsSolved)
                OfferDetail("Benefits and selling points", profile.benefits)
                OfferDetail("Pricing", profile.pricing)
                OfferDetail("What makes it different", profile.differentiators)
                OfferDetail("Objections and suggested responses", profile.objectionsAndResponses)
                OfferDetail("Preferred tone", profile.preferredTone)
                OfferDetail("Additional AI instructions", profile.aiInstructions)
            } },
            confirmButton = { TextButton(onClick = { editing = profile; selected = null; viewModel.clearError() }) { Text("Edit") } },
            dismissButton = { Row {
                if (!profile.isActive) TextButton(onClick = { viewModel.setActive(profile); selected = null }) { Text("Set active") }
                TextButton(onClick = { deleting = profile; selected = null }) { Text("Delete", color = Color(0xFFFF8A80)) }
                TextButton(onClick = { selected = null }) { Text("Done") }
            } },
            containerColor = card,
        )
    }
    deleting?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete offer profile?") },
            text = { Text("${profile.offerName} and its saved details will be removed from this device.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(profile) { deleting = null }; deleting = null }) { Text("Delete", color = Color(0xFFFF8A80)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun OfferProfileForm(profile: OfferProfile?, isSaving: Boolean, error: String?, onSave: (OfferProfile) -> Unit, onCancel: () -> Unit) {
    var draft by remember(profile?.id) { mutableStateOf(profile ?: OfferProfile(offerName = "", providerName = "")) }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel) { Text("‹  Back") }
            Spacer(Modifier.width(6.dp))
            Text(if (profile == null) "New offer profile" else "Edit offer profile", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("Add the context you want to keep ready for this offer.", fontSize = 13.sp, color = muted)
        Spacer(Modifier.height(10.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            OfferField("Offer or service name *", draft.offerName, { draft = draft.copy(offerName = it) }, "e.g. Social media management")
            OfferField("Business or provider name *", draft.providerName, { draft = draft.copy(providerName = it) }, "Your business name")
            OfferField("Description of the service", draft.description, { draft = draft.copy(description = it) }, "What does this service include?", 2)
            OfferField("Target customers", draft.targetCustomers, { draft = draft.copy(targetCustomers = it) }, "Who is this a good fit for?", 2)
            OfferField("Problems the service solves", draft.problemsSolved, { draft = draft.copy(problemsSolved = it) }, "What challenges does it address?", 2)
            OfferField("Main benefits and selling points", draft.benefits, { draft = draft.copy(benefits = it) }, "What value do customers get?", 2)
            OfferField("Pricing information (optional)", draft.pricing, { draft = draft.copy(pricing = it) }, "Price, packages, or billing details", 2)
            OfferField("What makes this offer different", draft.differentiators, { draft = draft.copy(differentiators = it) }, "Your approach, experience, or edge", 2)
            OfferField("Common objections and suggested responses", draft.objectionsAndResponses, { draft = draft.copy(objectionsAndResponses = it) }, "Objection → suggested response", 3)
            OfferField("Preferred sales or conversation tone", draft.preferredTone, { draft = draft.copy(preferredTone = it) }, "e.g. Warm, concise, consultative", 2)
            OfferField("Additional instructions for the AI", draft.aiInstructions, { draft = draft.copy(aiInstructions = it) }, "Any other guidance to follow", 3)
            if (error != null) Text(error, color = Color(0xFFFF8A80), fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 4.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) { Text("Cancel") }
            Button(onClick = { val now = System.currentTimeMillis(); onSave(draft.copy(offerName = draft.offerName.trim(), providerName = draft.providerName.trim(), description = draft.description.trim(), targetCustomers = draft.targetCustomers.trim(), problemsSolved = draft.problemsSolved.trim(), benefits = draft.benefits.trim(), pricing = draft.pricing.trim(), differentiators = draft.differentiators.trim(), objectionsAndResponses = draft.objectionsAndResponses.trim(), preferredTone = draft.preferredTone.trim(), aiInstructions = draft.aiInstructions.trim(), createdAt = profile?.createdAt ?: now, updatedAt = now)) }, modifier = Modifier.weight(1f), enabled = !isSaving) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save profile")
            }
        }
    }
}

@Composable
private fun OfferField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    Column {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
        OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, minLines = minLines, shape = RoundedCornerShape(14.dp))
    }
}

@Composable
private fun OfferDetail(label: String, value: String) {
    if (value.isBlank()) return
    Column(Modifier.padding(top = 10.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = mint, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 13.sp)
    }
}
