package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.currency.CurrencyManager
import com.example.data.local.seed.DefaultGamesData
import com.example.data.security.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GAMER BAZAAR", appName)
    }

    @Test
    fun `currency converter calculates non-equal parity between INR and USD`() {
        val usdAmount = 100.0
        val inrAmount = CurrencyManager.convert(usdAmount, "USD", "INR")
        // 100 USD at ~83.5 INR/USD should be around 8350 INR
        assertTrue("INR amount should be greater than 8000 for 100 USD", inrAmount > 8000.0)
        assertNotEquals(usdAmount, inrAmount)
    }

    @Test
    fun `game catalog contains at least 350 distinct titles`() {
        assertTrue("Game catalog must have at least 350 games", DefaultGamesData.ALL_GAMES.size >= 350)
    }

    @Test
    fun `security utils encrypts and decrypts delivery vault data`() {
        val originalSecret = "SUPERCELL_ID_TRANSFER_CODE: 994-112-X"
        val encrypted = SecurityUtils.encryptDeliveryData(originalSecret)
        assertNotEquals(originalSecret, encrypted)

        val decrypted = SecurityUtils.decryptDeliveryData(encrypted)
        assertEquals(originalSecret, decrypted)
    }

    @Test
    fun `password hashing creates consistent verification with salt`() {
        val password = "StrongPassword@2026"
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)

        assertTrue(SecurityUtils.verifyPassword(password, salt, hash))
        assertNotEquals("WrongPassword", SecurityUtils.verifyPassword("WrongPassword", salt, hash))
    }

    @Test
    fun `currency list contains all requested major currencies`() {
        val codes = CurrencyManager.SUPPORTED_CURRENCIES.map { it.code }
        val requiredCurrencies = listOf("INR", "USD", "EUR", "GBP", "RON", "AED", "SAR", "BDT", "PKR", "NPR")
        for (currency in requiredCurrencies) {
            assertTrue("Supported currencies should include $currency", codes.contains(currency))
        }
    }

    @Test
    fun `region list contains all requested region options`() {
        val requiredRegions = listOf("Global", "India", "Asia", "Europe", "North America", "South America", "Middle East", "Other")
        for (reg in requiredRegions) {
            assertTrue("Listing regions should include $reg", com.example.ui.screens.LISTING_REGION_OPTIONS.contains(reg))
        }
    }

    @Test
    fun `listing repository persists selected currency and region`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = com.example.data.repository.ListingRepository(context)

        val createdResult = repo.createListing(
            sellerId = "test_seller_1",
            sellerName = "ProGamer",
            gameId = "game_coc",
            gameTitle = "Clash of Clans",
            title = "TH16 Maxed Account",
            description = "High end base with all max heroes",
            originalPrice = 8500.0,
            originalCurrency = "INR",
            accountLevel = 245,
            rank = "Titan I",
            skinsCount = 35,
            charactersCount = 4,
            itemsDescription = "Max builder base included",
            region = "India",
            imageUrls = listOf("https://images.unsplash.com/test.jpg"),
            deliveryCredentialsPlain = "supercell_id: test@domain.com",
            transferInstructions = "Connect to supercell id",
            autoApprove = true
        )

        assertTrue(createdResult.isSuccess)
        val created = createdResult.getOrThrow()
        assertEquals("INR", created.originalCurrency)
        assertEquals("India", created.region)

        // Read back from database
        val fetched = repo.getListingById(created.id)
        assertTrue("Listing should be found in DB", fetched != null)
        assertEquals("INR", fetched?.originalCurrency)
        assertEquals("India", fetched?.region)

        // Update with EUR and Europe
        val updated = fetched!!.copy(
            originalCurrency = "EUR",
            originalPrice = 95.0,
            region = "Europe"
        )
        repo.updateListing(updated)

        val refetched = repo.getListingById(created.id)
        assertEquals("EUR", refetched?.originalCurrency)
        assertEquals("Europe", refetched?.region)
    }

    @Test
    fun `brawl stars config does not contain clash of clans examples`() {
        val bsConfig = com.example.ui.screens.getGameFormConfig("Brawl Stars")
        // Verify Brawl Stars has its own examples and labels
        assertTrue("Brawl Stars title should mention Brawlers or Trophies", bsConfig.titlePlaceholder.contains("Brawlers") || bsConfig.titlePlaceholder.contains("Trophies"))
        assertEquals("Brawlers", bsConfig.charactersLabel)
        assertEquals("Trophies", bsConfig.levelLabel)
        assertTrue(bsConfig.itemsLabel.contains("Hypercharges") || bsConfig.itemsLabel.contains("Star Powers"))

        // Verify NO Clash of Clans specific strings exist
        assertTrue("Should not contain TH16", !bsConfig.titlePlaceholder.contains("TH16"))
        assertTrue("Should not contain Town Hall", !bsConfig.levelLabel.contains("Town Hall"))
        assertTrue("Should not contain Heroes", !bsConfig.charactersLabel.contains("Heroes"))
    }

    @Test
    fun `game form configurations adapt for multiple games`() {
        val cocConfig = com.example.ui.screens.getGameFormConfig("Clash of Clans")
        assertTrue(cocConfig.titlePlaceholder.contains("TH16"))
        assertEquals("Town Hall (TH)", cocConfig.levelLabel)
        assertEquals("Heroes Count", cocConfig.charactersLabel)

        val crConfig = com.example.ui.screens.getGameFormConfig("Clash Royale")
        assertTrue(crConfig.titlePlaceholder.contains("Trophies"))
        assertEquals("Max Cards", crConfig.charactersLabel)

        val ffConfig = com.example.ui.screens.getGameFormConfig("Free Fire")
        assertTrue(ffConfig.titlePlaceholder.contains("Rare Bundles"))
        assertEquals("Characters", ffConfig.charactersLabel)

        val valConfig = com.example.ui.screens.getGameFormConfig("Valorant")
        assertTrue(valConfig.titlePlaceholder.contains("Immortal"))
        assertEquals("Agents", valConfig.charactersLabel)

        val fnConfig = com.example.ui.screens.getGameFormConfig("Fortnite")
        assertTrue(fnConfig.titlePlaceholder.contains("Rare Skins"))
        assertEquals("Pickaxes", fnConfig.charactersLabel)
    }
}
