package com.rateel.app.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiParsingTest {
    @Serializable
    private data class Fixture(val id: Int, val name: String)

    @Test
    fun parser_ignores_provider_fields_unknown_to_current_client() {
        val json = Json { ignoreUnknownKeys = true }
        val parsed = json.decodeFromString<Fixture>(
            """{"id":7,"name":"radio","future_field":{"value":1}}""",
        )
        assertEquals(7, parsed.id)
        assertEquals("radio", parsed.name)
    }
}
