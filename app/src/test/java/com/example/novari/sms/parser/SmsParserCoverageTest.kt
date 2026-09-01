package com.example.novari.sms.parser

import com.example.novari.core.model.TransactionType
import com.example.novari.sms.model.RawSmsMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Runs the fixture corpus under [FinancialSmsParser] and asserts a coverage floor, so a regex
 * change that fixes one issuer but silently breaks another fails the build instead of a bug
 * report. Every new fixture file in `resources/sms-fixtures/<issuer>.json` is picked up
 * automatically -- add a fixture before fixing a reported parsing bug.
 */
class SmsParserCoverageTest {

    private val parser = FinancialSmsParser()

    private data class Fixture(
        val issuer: String,
        val sender: String,
        val body: String,
        val shouldParse: Boolean,
        val amountMinor: Long?,
        val transactionType: TransactionType?,
        val merchant: String?,
        val referenceNumber: String?
    )

    private fun loadFixtures(): List<Fixture> {
        val fixturesDir = File(
            requireNotNull(javaClass.classLoader?.getResource("sms-fixtures")) {
                "sms-fixtures resource directory not found on the test classpath"
            }.file
        )
        val fixtures = mutableListOf<Fixture>()
        for (file in fixturesDir.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }) {
            val issuer = file.nameWithoutExtension
            val cases = Json.parseToJsonElement(file.readText()).jsonArray
            for (case in cases) {
                val obj = case.jsonObject
                fixtures += Fixture(
                    issuer = issuer,
                    sender = obj["sender"]!!.jsonPrimitive.content,
                    body = obj["body"]!!.jsonPrimitive.content,
                    shouldParse = obj["shouldParse"]!!.jsonPrimitive.content.toBoolean(),
                    amountMinor = obj["amountMinor"]?.jsonPrimitive?.longOrNull,
                    transactionType = obj["transactionType"]?.jsonPrimitive?.content?.let(TransactionType::valueOf),
                    merchant = obj["merchant"]?.jsonPrimitive?.content,
                    referenceNumber = obj["referenceNumber"]?.jsonPrimitive?.content
                )
            }
        }
        return fixtures
    }

    @Test
    fun `parser coverage floor across the fixture corpus`() {
        val fixtures = loadFixtures()
        assertTrue("Fixture corpus is empty -- add fixtures under resources/sms-fixtures/", fixtures.isNotEmpty())

        val failures = mutableListOf<String>()
        for (fixture in fixtures) {
            val parsed = parser.parse(RawSmsMessage(sender = fixture.sender, body = fixture.body, timestamp = 1_700_000_000_000L))
            val mismatch = when {
                fixture.shouldParse && parsed == null -> "expected a parse, got null"
                !fixture.shouldParse && parsed != null -> "expected no parse, got $parsed"
                fixture.shouldParse && parsed != null -> listOfNotNull(
                    fixture.amountMinor?.takeIf { it != parsed.amountMinor }
                        ?.let { "amountMinor expected $it got ${parsed.amountMinor}" },
                    fixture.transactionType?.takeIf { it != parsed.transactionType }
                        ?.let { "transactionType expected $it got ${parsed.transactionType}" },
                    fixture.merchant?.takeIf { it != parsed.merchant }
                        ?.let { "merchant expected $it got ${parsed.merchant}" },
                    fixture.referenceNumber?.takeIf { it != parsed.referenceNumber }
                        ?.let { "referenceNumber expected $it got ${parsed.referenceNumber}" }
                ).joinToString("; ").ifBlank { null }
                else -> null
            }
            if (mismatch != null) {
                failures += "[${fixture.issuer}] \"${fixture.body.take(60)}...\" -> $mismatch"
            }
        }

        val passRate = (fixtures.size - failures.size).toDouble() / fixtures.size
        val report = "Parser coverage ${"%.1f".format(passRate * 100)}% " +
            "(${fixtures.size - failures.size}/${fixtures.size}). Failures:\n" + failures.joinToString("\n")

        assertTrue(report, passRate >= COVERAGE_FLOOR)
    }

    companion object {
        private const val COVERAGE_FLOOR = 0.95
    }
}
