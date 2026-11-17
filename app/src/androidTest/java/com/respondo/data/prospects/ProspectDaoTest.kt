package com.respondo.data.prospects

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProspectDaoTest {
    private lateinit var database: RespondoDatabase
    private lateinit var dao: ProspectDao

    @Before fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RespondoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.prospectDao()
    }

    @After fun closeDatabase() = database.close()

    @Test fun prospectCanBeInsertedQueriedUpdatedAndDeleted() = runBlocking {
        val original = sampleProspect()
        val id = dao.insert(original)
        val inserted = dao.findById(id)
        assertEquals(original.fullName, inserted?.fullName)

        dao.update(requireNotNull(inserted).copy(businessName = "Updated Studio"))
        assertEquals("Updated Studio", dao.findById(id)?.businessName)
        assertEquals(1, dao.observeAll().first().size)

        dao.delete(requireNotNull(dao.findById(id)))
        assertNull(dao.findById(id))
        assertEquals(0, dao.observeAll().first().size)
    }

    private fun sampleProspect() = Prospect(
        fullName = "Jordan Lee", businessName = "Northstar Studio", phoneNumber = "+1 555 123 4567",
        businessDescription = "Design studio", problemOrWeakness = "Limited lead follow-up",
        howICanHelp = "Set up a repeatable process", serviceOrOffer = "Sales consulting", additionalNotes = "Prefers email",
    )
}
