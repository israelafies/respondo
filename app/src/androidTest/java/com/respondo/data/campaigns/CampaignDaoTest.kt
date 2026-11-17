package com.respondo.data.campaigns

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.respondo.data.prospects.Prospect
import com.respondo.data.prospects.RespondoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampaignDaoTest {
    private lateinit var db: RespondoDatabase
    private lateinit var campaignDao: CampaignDao

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RespondoDatabase::class.java).allowMainThreadQueries().build()
        campaignDao = db.campaignDao()
    }
    @After fun close() = db.close()

    @Test fun campaignCanBeCreatedAndReceivesProspectMembership() = runBlocking {
        val campaignId = campaignDao.insertCampaign(Campaign(name = "Launch"))
        val prospectId = db.prospectDao().insert(sampleProspect())
        assertTrue(campaignDao.addMemberIfAbsent(campaignId, prospectId))
        assertEquals(1, campaignDao.observeMembers(campaignId).first().size)
        assertEquals(1, campaignDao.observeTotal(campaignId).first())
    }

    @Test fun duplicateMembershipIsIgnoredAndProspectCanJoinMultipleCampaigns() = runBlocking {
        val first = campaignDao.insertCampaign(Campaign(name = "First"))
        val second = campaignDao.insertCampaign(Campaign(name = "Second"))
        val prospectId = db.prospectDao().insert(sampleProspect())
        assertTrue(campaignDao.addMemberIfAbsent(first, prospectId))
        assertFalse(campaignDao.addMemberIfAbsent(first, prospectId))
        assertTrue(campaignDao.addMemberIfAbsent(second, prospectId))
        assertEquals(1, campaignDao.observeMembers(first).first().size)
        assertEquals(1, campaignDao.observeMembers(second).first().size)
    }

    @Test fun stageUpdatesAndDeletingCampaignPreservesProspect() = runBlocking {
        val campaignId = campaignDao.insertCampaign(Campaign(name = "Follow up"))
        val prospectId = db.prospectDao().insert(sampleProspect())
        campaignDao.addMemberIfAbsent(campaignId, prospectId)
        campaignDao.updateStage(campaignId, prospectId, SalesStage.NEGOTIATING)
        assertEquals(SalesStage.NEGOTIATING, campaignDao.findMember(campaignId, prospectId)?.stage)
        campaignDao.deleteCampaign(Campaign(id = campaignId, name = "Follow up"))
        assertTrue(campaignDao.observeMembers(campaignId).first().isEmpty())
        assertEquals("Jordan Lee", db.prospectDao().findById(prospectId)?.fullName)
        assertNull(campaignDao.findMember(campaignId, prospectId))
    }

    private fun sampleProspect() = Prospect(fullName = "Jordan Lee", businessName = "Northstar Studio", phoneNumber = "5551234567", businessDescription = "Design studio", problemOrWeakness = "Follow-up", howICanHelp = "Sales process", serviceOrOffer = "Consulting", additionalNotes = "")
}
