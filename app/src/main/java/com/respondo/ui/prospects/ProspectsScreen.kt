package com.respondo.ui.prospects

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.respondo.data.prospects.Prospect

@Composable
fun ProspectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProspectViewModel,
    initiallyAdding: Boolean = false,
) {
    val state by viewModel.uiState.collectAsState()
    var editing by remember { mutableStateOf<Prospect?>(null) }
    var adding by rememberSaveable(initiallyAdding) { mutableStateOf(initiallyAdding) }
    var selected by remember { mutableStateOf<Prospect?>(null) }
    var deleting by remember { mutableStateOf<Prospect?>(null) }

    when {
        adding || editing != null -> ProspectForm(
            prospect = editing,
            isSaving = state.isSaving,
            error = state.error,
            onSave = { viewModel.save(it) { adding = false; editing = null } },
            onCancel = { adding = false; editing = null; viewModel.clearError() },
        )
        else -> Column(modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Prospects", fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Text("Your people and the opportunities ahead.", fontSize = 13.sp, color = Color(0xFFA5B5AE))
                }
                Button(onClick = { adding = true; viewModel.clearError() }, shape = RoundedCornerShape(14.dp)) {
                    Text("＋  Add")
                }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::search,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search name, business or phone") },
                leadingIcon = { Text("⌕", fontSize = 22.sp) },
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(14.dp))
            state.error?.let { message ->
                Text(message, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            if (state.prospects.isEmpty()) {
                EmptyProspects(hasSearch = state.query.isNotBlank(), onAdd = { adding = true })
            } else {
                Text("${state.prospects.size} ${if (state.prospects.size == 1) "PROSPECT" else "PROSPECTS"}", fontSize = 10.sp, letterSpacing = 1.3.sp, color = Color(0xFFA5B5AE))
                Spacer(Modifier.height(9.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxSize()) {
                    items(state.prospects, key = { it.id }) { prospect ->
                        ProspectCard(prospect, onClick = { selected = prospect })
                    }
                }
            }
        }
    }

    selected?.let { prospect ->
        ProspectDetailsDialog(
            prospect = prospect,
            onDismiss = { selected = null },
            onEdit = { editing = prospect; selected = null; viewModel.clearError() },
            onDelete = { deleting = prospect; selected = null },
        )
    }
    deleting?.let { prospect ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete prospect?") },
            text = { Text("${prospect.fullName} and their saved details will be removed from this device.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(prospect) { deleting = null }; deleting = null }) { Text("Delete", color = Color(0xFFFF8A80)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EmptyProspects(hasSearch: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 35.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFF15201D)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF202C28)), contentAlignment = Alignment.Center) { Text(if (hasSearch) "⌕" else "◎", fontSize = 25.sp, color = Color(0xFF8CE0C0)) }
        Spacer(Modifier.height(14.dp))
        Text(if (hasSearch) "No matches found" else "Your next opportunity starts here", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(if (hasSearch) "Try another name, business or phone number." else "Keep your business context close. Add a prospect to get started.", color = Color(0xFFA5B5AE), fontSize = 13.sp)
        if (!hasSearch) {
            Spacer(Modifier.height(17.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(13.dp)) { Text("＋  Add your first prospect") }
        }
    }
}

@Composable
private fun ProspectCard(prospect: Prospect, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF15201D))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF202C28)), contentAlignment = Alignment.Center) {
                Text(prospect.fullName.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""), color = Color(0xFF8CE0C0), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(prospect.fullName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text(prospect.businessName, color = Color(0xFFA5B5AE), fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text(prospect.phoneNumber, color = Color(0xFF8CE0C0), fontSize = 12.sp)
            }
            Text("›", color = Color(0xFFA5B5AE), fontSize = 24.sp)
        }
    }
}

@Composable
private fun ProspectForm(prospect: Prospect?, isSaving: Boolean, error: String?, onSave: (Prospect) -> Unit, onCancel: () -> Unit) {
    var fullName by remember(prospect?.id) { mutableStateOf(prospect?.fullName.orEmpty()) }
    var business by remember(prospect?.id) { mutableStateOf(prospect?.businessName.orEmpty()) }
    var phone by remember(prospect?.id) { mutableStateOf(prospect?.phoneNumber.orEmpty()) }
    var description by remember(prospect?.id) { mutableStateOf(prospect?.businessDescription.orEmpty()) }
    var problem by remember(prospect?.id) { mutableStateOf(prospect?.problemOrWeakness.orEmpty()) }
    var help by remember(prospect?.id) { mutableStateOf(prospect?.howICanHelp.orEmpty()) }
    var offer by remember(prospect?.id) { mutableStateOf(prospect?.serviceOrOffer.orEmpty()) }
    var notes by remember(prospect?.id) { mutableStateOf(prospect?.additionalNotes.orEmpty()) }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel) { Text("‹  Back") }
            Spacer(Modifier.width(6.dp))
            Text(if (prospect == null) "New prospect" else "Edit prospect", fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("A little context makes every follow-up more personal.", fontSize = 13.sp, color = Color(0xFFA5B5AE))
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField("Full name *", fullName, { fullName = it }, "Contact person")
            FormField("Business name *", business, { business = it }, "Company or business")
            FormField("Phone number *", phone, { phone = it }, "+1 555 000 0000")
            FormField("Business description", description, { description = it }, "What does the business do?", minLines = 2)
            FormField("Problem or weakness identified", problem, { problem = it }, "What could be improved?", minLines = 2)
            FormField("How I can help", help, { help = it }, "Describe the value you can bring.", minLines = 2)
            FormField("Service or offer being sold", offer, { offer = it }, "Your service or offer", minLines = 2)
            FormField("Additional notes", notes, { notes = it }, "Anything else to remember?", minLines = 2)
            if (error != null) Text(error, color = Color(0xFFFF8A80), fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) { Text("Cancel") }
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(Prospect(
                        id = prospect?.id ?: 0,
                        fullName = fullName.trim(), businessName = business.trim(), phoneNumber = phone.trim(),
                        businessDescription = description.trim(), problemOrWeakness = problem.trim(), howICanHelp = help.trim(),
                        serviceOrOffer = offer.trim(), additionalNotes = notes.trim(),
                        createdAt = prospect?.createdAt ?: now, updatedAt = now,
                    ))
                },
                modifier = Modifier.weight(1f), enabled = !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save prospect")
            }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    Column {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
        OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, minLines = minLines, shape = RoundedCornerShape(14.dp))
    }
}

@Composable
private fun ProspectDetailsDialog(prospect: Prospect, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Column {
            Text(prospect.fullName, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(prospect.businessName, color = Color(0xFFA5B5AE), fontSize = 14.sp)
        } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                DetailRow("Phone", prospect.phoneNumber)
                DetailRow("Business", prospect.businessDescription)
                DetailRow("Opportunity", prospect.problemOrWeakness)
                DetailRow("How I can help", prospect.howICanHelp)
                DetailRow("Offer", prospect.serviceOrOffer)
                DetailRow("Notes", prospect.additionalNotes)
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Edit") } },
        dismissButton = { Row {
            TextButton(onClick = onDelete) { Text("Delete", color = Color(0xFFFF8A80)) }
            TextButton(onClick = onDismiss) { Text("Done") }
        } },
        containerColor = Color(0xFF15201D),
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(Modifier.padding(top = 10.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = Color(0xFF8CE0C0), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 13.sp)
    }
}
