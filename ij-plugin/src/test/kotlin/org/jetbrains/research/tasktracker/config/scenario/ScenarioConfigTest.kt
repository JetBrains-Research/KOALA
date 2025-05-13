package org.jetbrains.research.tasktracker.config.scenario

import org.jetbrains.research.tasktracker.config.YamlConfigLoadStrategy
import org.jetbrains.research.tasktracker.config.scenario.models.TaskUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class ScenarioConfigTest {
    private fun loadTestScenario(): String {
        return ScenarioConfigTest::class.java
            .getResource("testScenario.yaml")?.readText()
            ?: error("Cannot find testScenario.yaml!")
    }

    @Test
    fun testTimerSecondsParsingFromConfig() {
        // Parse the YAML into a ScenarioConfig object
        val scenarioConfig = YamlConfigLoadStrategy.load(loadTestScenario(), ScenarioConfig.serializer())

        // Get the first step from the scenario
        val step = scenarioConfig.scenario.steps.peek()

        // Get the units from the step
        val units = step.getUnits()

        // Find the task unit with id "timed_task"
        val timedTaskUnit = units.find { it is TaskUnit && it.id == "timed_task" } as TaskUnit

        // Verify that the timer seconds value is correctly parsed
        assertEquals(60L, timedTaskUnit.timerSeconds)
    }

    @Test
    fun testNoTimerIsNull() {
        // Parse the YAML into a ScenarioConfig object
        val scenarioConfig = YamlConfigLoadStrategy.load(loadTestScenario(), ScenarioConfig.serializer())

        // Get the first step from the scenario
        val step = scenarioConfig.scenario.steps.peek()

        // Get the units from the step
        val units = step.getUnits()

        // Find the task unit with id "main" (which has no timer)
        val mainTaskUnit = units.find { it is TaskUnit && it.id == "main" } as TaskUnit

        // Verify that the timer seconds value is null
        assertNull(mainTaskUnit.timerSeconds)
    }
}
