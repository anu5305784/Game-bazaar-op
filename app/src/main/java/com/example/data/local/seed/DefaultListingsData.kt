package com.example.data.local.seed

import com.example.data.local.entities.DeliveryVaultEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.security.SecurityUtils

object DefaultListingsData {
    val INITIAL_LISTINGS: List<ListingEntity> = listOf(
        ListingEntity(
            id = "lst_coc_th16",
            sellerId = "seller_apex_trader",
            sellerName = "ApexTrader_Official",
            sellerRating = 4.98f,
            gameId = "g_coc",
            gameTitle = "Clash of Clans",
            title = "Town Hall 16 Max Defense | 95/95/70/45 Heroes | 1200+ War Stars | Free Name Change",
            description = "High-tier competitive account. Fully maxed Town Hall 16 with Queen 95, King 95, Warden 70, Champion 45. Scenery unlocked, 45 Hero skins, 1,450 CWL Medals, high gem count. Safe Supercell ID transfer ready.",
            originalPrice = 14500.0,
            originalCurrency = "INR",
            accountLevel = 245,
            rank = "Town Hall 16 Max",
            skinsCount = 45,
            charactersCount = 4,
            itemsDescription = "1450 CWL medals, 6 Runes, 12 Books, 3,200 Gems",
            region = "Global",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800,https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 2
        ),
        ListingEntity(
            id = "lst_val_radiant",
            sellerId = "seller_val_pro",
            sellerName = "ViperElite",
            sellerRating = 4.95f,
            gameId = "g_val",
            gameTitle = "Valorant",
            title = "Radiant Peak Rank | Champions 2021 Vandal + Prime Karambit + Kuronami Bundle",
            description = "Exclusive competitive account with rare discontinued skins. All agents unlocked, Episode 6 & 7 Radiant gun buddy, Reaver Vandal, Prime 2.0, Kuronami bundle fully upgraded with Radianite points.",
            originalPrice = 280.0,
            originalCurrency = "USD",
            accountLevel = 188,
            rank = "Radiant (Peak)",
            skinsCount = 68,
            charactersCount = 24,
            itemsDescription = "Champions 2021 Vandal, Kuronami Melee, 240 Radianite",
            region = "North America",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800,https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 4
        ),
        ListingEntity(
            id = "lst_bgmi_glacier",
            sellerId = "seller_bgmi_king",
            sellerName = "KolkataGamer",
            sellerRating = 4.92f,
            gameId = "g_bgmi",
            gameTitle = "BGMI",
            title = "M416 Glacier Level 7 (On-Hit + Loot Crate) | Conqueror Title C3S8 | Fool Set Max",
            description = "Genuine BGMI personal account. Glacier M416 maxed out with kill broadcast and final loot crate. Multiple X-Suits, Conqueror frame, Mythic Fashion badge. Clean Twitter/Google link available for transfer.",
            originalPrice = 22000.0,
            originalCurrency = "INR",
            accountLevel = 74,
            rank = "Conqueror C3S8",
            skinsCount = 140,
            charactersCount = 8,
            itemsDescription = "M416 Glacier Lvl 7, Pharaoh X-Suit 4-Star, Fool M416",
            region = "India",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 6
        ),
        ListingEntity(
            id = "lst_genshin_c6",
            sellerId = "seller_gacha_god",
            sellerName = "TeyvatVault",
            sellerRating = 5.0f,
            gameId = "g_genshin",
            gameTitle = "Genshin Impact",
            title = "AR 60 Whale Account | C6 R5 Raiden Shogun + C6 Furina + C6 Nahida + 30 5-Stars",
            description = "Day-1 Asia server endgame account. Over 30 signature 5-star weapons, all archons obtained, massive artifact inventory with 45+ CV on major sets. 100% exploration on Mondstadt to Fontaine.",
            originalPrice = 450.0,
            originalCurrency = "EUR",
            accountLevel = 60,
            rank = "Adventure Rank 60",
            skinsCount = 18,
            charactersCount = 38,
            itemsDescription = "C6 Raiden, C6 Furina, C6 Nahida, 32 Signature Weapons",
            region = "Asia",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 8
        ),
        ListingEntity(
            id = "lst_cs2_gloves",
            sellerId = "seller_cs_skins",
            sellerName = "NordicCS2",
            sellerRating = 4.96f,
            gameId = "g_cs2",
            gameTitle = "Counter-Strike 2",
            title = "CS2 Prime Steam Account | Butterfly Knife Doppler Phase 2 + Sport Gloves Vice",
            description = "Steam account with CS2 Premier Rating 19,500+. High trust factor, 1,800+ hours, genuine inventory with Butterfly Knife Doppler P2 and Vice Gloves Field-Tested. Instant delivery with original email credentials.",
            originalPrice = 950.0,
            originalCurrency = "GBP",
            accountLevel = 45,
            rank = "Premier 19,500+",
            skinsCount = 52,
            charactersCount = 0,
            itemsDescription = "Butterfly Doppler P2, Vice Gloves, AK-47 Vulcan MW",
            region = "Europe",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 10
        ),
        ListingEntity(
            id = "lst_mlbb_mythic",
            sellerId = "seller_sea_legend",
            sellerName = "ManilaGamer99",
            sellerRating = 4.88f,
            gameId = "g_mlbb",
            gameTitle = "Mobile Legends: Bang Bang",
            title = "Mythical Immortal 120 Stars | 12 Collector Skins + 4 Legend Skins (Gusion, Lesley)",
            description = "High win-rate SEA server MLBB account. 78% overall winrate across 4,000 matches. Gusion Cosmic Gleam, Lesley Angelic Agent, Chou Iori Yagami KOF. Moonton account clean bind.",
            originalPrice = 750.0,
            originalCurrency = "AED",
            accountLevel = 90,
            rank = "Mythical Immortal 120★",
            skinsCount = 380,
            charactersCount = 125,
            itemsDescription = "4 Legend Skins, 12 Collector Skins, KOF Chou & Guinevere",
            region = "Southeast Asia",
            status = "APPROVED",
            isFeatured = false,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 12
        ),
        ListingEntity(
            id = "lst_roblox_korblox",
            sellerId = "seller_rbx_vault",
            sellerName = "BloxEmpire",
            sellerRating = 4.90f,
            gameId = "g_roblox",
            gameTitle = "Roblox",
            title = "OG 2017 Roblox Account | Korblox Deathspeaker + Headless Horseman + 15k Robux",
            description = "Clean verified 2017 Roblox profile. Includes the rare Korblox Deathspeaker package, Headless Horseman avatar, thousands of accessories, and 15,000 clean pending Robux.",
            originalPrice = 320.0,
            originalCurrency = "USD",
            accountLevel = 100,
            rank = "OG Verified 2017",
            skinsCount = 210,
            charactersCount = 1,
            itemsDescription = "Korblox Deathspeaker, Headless Horseman, 15,000 Robux",
            region = "Global",
            status = "APPROVED",
            isFeatured = false,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 14
        ),
        ListingEntity(
            id = "lst_brawlstars_pro",
            sellerId = "seller_supercell_hub",
            sellerName = "StarPowerSeller",
            sellerRating = 4.94f,
            gameId = "g_bs",
            gameTitle = "Brawl Stars",
            title = "75,000 Trophies | All 82 Brawlers Unlocked & Power 11 | 28 Hypercharges + Masters",
            description = "Ranked Masters tier Brawl Stars account. Star Shelly OG skin, Challenger Colt, Phoenix Crow, Mecha Mortis. 28 Hypercharges unlocked and maxed gears. Ready for instant Supercell ID transfer.",
            originalPrice = 850.0,
            originalCurrency = "RON",
            accountLevel = 210,
            rank = "Masters Rank / 75k",
            skinsCount = 145,
            charactersCount = 82,
            itemsDescription = "Star Shelly (2018), 28 Hypercharges, 40,000 Star Points",
            region = "Europe",
            status = "APPROVED",
            isFeatured = false,
            isShowcase = false,
            imageUrlsJson = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 16
        ),
        ListingEntity(
            id = "lst_freefire_sakura",
            sellerId = "seller_ff_pro",
            sellerName = "GamerBazaarShowcase",
            sellerRating = 5.0f,
            gameId = "g_ff",
            gameTitle = "Free Fire",
            title = "Season 1 Sakura Elite Pass + Season 2 Hip Hop Bundle | MP40 Predatory Cobra Max",
            description = "Showcase model demonstrating how elite legacy accounts are cataloged on Gamer Bazaar. Features the ultra-rare Season 1 Sakura bundle and Evo gun skins.",
            originalPrice = 350.0,
            originalCurrency = "USD",
            accountLevel = 85,
            rank = "Grandmaster Top 100",
            skinsCount = 280,
            charactersCount = 45,
            itemsDescription = "S1 Sakura Bundle, S2 Hip Hop Bundle, Evo MP40 Max",
            region = "Global",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = true, // Showcase example: not purchasable, explained upon opening
            imageUrlsJson = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 18
        ),
        ListingEntity(
            id = "lst_fortnite_renegade",
            sellerId = "seller_fortnite_og",
            sellerName = "GamerBazaarShowcase",
            sellerRating = 5.0f,
            gameId = "g_fortnite",
            gameTitle = "Fortnite",
            title = "OG Season 1 Renegade Raider + Black Knight + Travis Scott + Galaxy Skin",
            description = "Showcase model demonstrating multi-platform cosmetic valuations on Gamer Bazaar. Features first-edition battle pass cosmetics, exclusive concert outfits, and device-tied skins.",
            originalPrice = 1200.0,
            originalCurrency = "USD",
            accountLevel = 1400,
            rank = "OG Chapter 1 Season 1",
            skinsCount = 340,
            charactersCount = 0,
            itemsDescription = "Renegade Raider, Black Knight, Travis Scott, Galaxy",
            region = "Global",
            status = "APPROVED",
            isFeatured = true,
            isShowcase = true, // Showcase example: not purchasable, explained upon opening
            imageUrlsJson = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800",
            createdAt = System.currentTimeMillis() - 3600000 * 20
        )
    )

    fun createInitialVaults(): List<DeliveryVaultEntity> {
        return INITIAL_LISTINGS.map { listing ->
            val credentialsPlain = if (listing.isShowcase) {
                "SHOWCASE_EXAMPLE: This is a demonstration account for visual layout presentation and is not available for transfer."
            } else {
                """
                GAME: ${listing.gameTitle}
                LOGIN_ID: seller_account_${listing.id}@gamerbazaar.net
                TEMPORARY_ACCESS_KEY: GBZ-${listing.id.takeLast(6).uppercase()}-X99
                SUPERCELL_ID_OR_AUTH: Registered to gamerbazaar-vault-${listing.id.takeLast(4)}@protect.net
                RECOVERY_SECURITY_CODE: 9812-4412-8723-0192
                INSTRUCTIONS:
                1. Download ${listing.gameTitle} on your device.
                2. Select Login via Supercell ID / Publisher Account.
                3. Enter the LOGIN_ID above.
                4. Use the temporary access code or check the protected email linked in your order dashboard.
                5. Immediately update password and link your personal secondary 2FA.
                """.trimIndent()
            }
            DeliveryVaultEntity(
                listingId = listing.id,
                encryptedCredentials = SecurityUtils.encryptDeliveryData(credentialsPlain),
                transferMethod = "Verified Direct Credentials & Email Access",
                transferInstructions = "Credentials will be automatically unlocked and visible in your My Purchases tab once payment is confirmed. Gamer Bazaar Escrow protects buyer funds until delivery is verified.",
                isReleased = false
            )
        }
    }
}
