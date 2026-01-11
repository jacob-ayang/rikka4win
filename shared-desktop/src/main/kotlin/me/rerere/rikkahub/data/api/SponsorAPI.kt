package me.rerere.rikkahub.data.api

import me.rerere.rikkahub.data.model.Sponsor

interface SponsorAPI {
    suspend fun getSponsors(): List<Sponsor>

    companion object {
        fun create(): SponsorAPI {
            return object : SponsorAPI {
                override suspend fun getSponsors(): List<Sponsor> = emptyList()
            }
        }
    }
}
