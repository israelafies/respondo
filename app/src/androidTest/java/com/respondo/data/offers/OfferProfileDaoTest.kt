package com.respondo.data.offers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.respondo.data.prospects.RespondoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfferProfileDaoTest {
    private lateinit var database: RespondoDatabase
    private lateinit var dao: OfferProfileDao

    @Before fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RespondoDatabase::class.java).allowMainThreadQueries().build()
        dao = database.offerProfileDao()
    }

    @After fun closeDatabase() = database.close()

    @Test fun multipleProfilesPersistAndOnlySelectedProfileIsActive() = runBlocking {
        val firstId = dao.insert(OfferProfile(offerName = "Consulting", providerName = "Northstar", pricing = "From 500 USD"))
        val secondId = dao.insert(OfferProfile(offerName = "Workshops", providerName = "Northstar"))
        dao.setActive(firstId)
        dao.setActive(secondId)

        val profiles = dao.observeAll().first()
        assertEquals(2, profiles.size)
        assertEquals(secondId, profiles.first().id)
        assertFalse(profiles.single { it.id == firstId }.isActive)
        assertTrue(profiles.single { it.id == secondId }.isActive)
        assertEquals("From 500 USD", profiles.single { it.id == firstId }.pricing)
    }
}
