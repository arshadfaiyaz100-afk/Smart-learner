package com.example.python

import com.example.data.model.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class PythonExecutionResult(
    val output: String,
    val error: String? = null,
    val executionTimeMs: Long = 0,
    val isSuccess: Boolean = true,
    val testCaseResults: List<TestCaseExecutionResult> = emptyList()
)

data class TestCaseExecutionResult(
    val name: String,
    val expected: String,
    val actual: String,
    val isPassed: Boolean
)

object PythonEngine {

    private const val MAX_STEPS = 10000
    private const val TIMEOUT_MS = 2500L

    suspend fun execute(
        code: String,
        testCases: List<TestCase> = emptyList()
    ): PythonExecutionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        val timeoutResult = withTimeoutOrNull(TIMEOUT_MS) {
            runSandboxedCode(code, testCases)
        }

        val elapsed = System.currentTimeMillis() - startTime

        if (timeoutResult == null) {
            PythonExecutionResult(
                output = "",
                error = "ExecutionError: Execution timed out (${TIMEOUT_MS}ms limit exceeded). Check for infinite loops.",
                executionTimeMs = elapsed,
                isSuccess = false
            )
        } else {
            timeoutResult.copy(executionTimeMs = elapsed)
        }
    }

    private fun runSandboxedCode(code: String, testCases: List<TestCase>): PythonExecutionResult {
        val stdout = StringBuilder()
        val env = mutableMapOf<String, Any?>()
        var stepCount = 0

        // Native safe functions
        val functions = mutableMapOf<String, (List<Any?>) -> Any?>()

        functions["len"] = { args ->
            when (val arg = args.firstOrNull()) {
                is String -> arg.length
                is List<*> -> arg.size
                is Map<*, *> -> arg.size
                else -> 0
            }
        }
        functions["str"] = { args -> args.firstOrNull()?.toString() ?: "None" }
        functions["int"] = { args ->
            val v = args.firstOrNull()?.toString()?.trim() ?: "0"
            v.toIntOrNull() ?: 0
        }
        functions["float"] = { args ->
            val v = args.firstOrNull()?.toString()?.trim() ?: "0"
            v.toDoubleOrNull() ?: 0.0
        }

        try {
            val lines = code.lines()
            var i = 0
            while (i < lines.size) {
                stepCount++
                if (stepCount > MAX_STEPS) {
                    return PythonExecutionResult(
                        output = stdout.toString(),
                        error = "RuntimeError: Maximum step limit ($MAX_STEPS) exceeded.",
                        isSuccess = false
                    )
                }

                val rawLine = lines[i]
                val trimmed = rawLine.trim()

                // Skip blanks and comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    i++
                    continue
                }

                // Handle print(...)
                if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
                    val inner = trimmed.substring(6, trimmed.length - 1)
                    val value = evaluateExpression(inner, env, functions)
                    stdout.appendLine(value?.toString() ?: "None")
                    i++
                    continue
                }

                // Handle assignments: var = expr
                if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.startsWith("if ") && !trimmed.startsWith("def ")) {
                    val eqIndex = trimmed.indexOf("=")
                    val varName = trimmed.substring(0, eqIndex).trim()
                    val expr = trimmed.substring(eqIndex + 1).trim()
                    if (isValidIdentifier(varName)) {
                        val value = evaluateExpression(expr, env, functions)
                        env[varName] = value
                    }
                    i++
                    continue
                }

                // Handle def function_name(...) basic interpreter capture
                if (trimmed.startsWith("def ")) {
                    val defRest = trimmed.substring(4)
                    val pOpen = defRest.indexOf("(")
                    val pClose = defRest.indexOf(")")
                    if (pOpen > 0 && pClose > pOpen) {
                        val fnName = defRest.substring(0, pOpen).trim()
                        val params = defRest.substring(pOpen + 1, pClose).split(",").map { it.trim() }.filter { it.isNotEmpty() }

                        // Collect function body lines (indented)
                        val bodyLines = mutableListOf<String>()
                        var j = i + 1
                        while (j < lines.size && (lines[j].startsWith("    ") || lines[j].startsWith("\t") || lines[j].isBlank())) {
                            if (lines[j].isNotBlank()) bodyLines.add(lines[j].trim())
                            j++
                        }
                        i = j

                        // Register custom function
                        functions[fnName] = { callArgs ->
                            val localEnv = HashMap(env)
                            params.forEachIndexed { idx, param ->
                                localEnv[param] = callArgs.getOrNull(idx)
                            }
                            var returnVal: Any? = null
                            for (bLine in bodyLines) {
                                if (bLine.startsWith("return ")) {
                                    val retExpr = bLine.substring(7).trim()
                                    returnVal = evaluateExpression(retExpr, localEnv, functions)
                                    break
                                } else if (bLine.contains("=") && !bLine.contains("==")) {
                                    val eqIdx = bLine.indexOf("=")
                                    val vName = bLine.substring(0, eqIdx).trim()
                                    val e = bLine.substring(eqIdx + 1).trim()
                                    localEnv[vName] = evaluateExpression(e, localEnv, functions)
                                }
                            }
                            returnVal
                        }
                        continue
                    }
                }

                // If none matched, attempt expression evaluation
                evaluateExpression(trimmed, env, functions)
                i++
            }

            // Run test cases if provided
            val testResults = mutableListOf<TestCaseExecutionResult>()
            for (tc in testCases) {
                val testOut = if (tc.input.isNotEmpty()) {
                    val res = evaluateExpression(tc.input, env, functions)
                    res?.toString() ?: ""
                } else {
                    stdout.toString().trim()
                }

                val passed = testOut.trim() == tc.expectedOutput.trim() ||
                        testOut.replace(" ", "").equals(tc.expectedOutput.replace(" ", ""), ignoreCase = true)

                testResults.add(
                    TestCaseExecutionResult(
                        name = tc.name,
                        expected = tc.expectedOutput,
                        actual = testOut,
                        isPassed = passed
                    )
                )
            }

            return PythonExecutionResult(
                output = stdout.toString().trimEnd(),
                error = null,
                isSuccess = testResults.all { it.isPassed },
                testCaseResults = testResults
            )

        } catch (e: Exception) {
            return PythonExecutionResult(
                output = stdout.toString(),
                error = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Syntax or runtime error"}",
                isSuccess = false
            )
        }
    }

    private fun evaluateExpression(
        expr: String,
        env: Map<String, Any?>,
        functions: Map<String, (List<Any?>) -> Any?>
    ): Any? {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return null

        // Literals
        if (trimmed == "True") return true
        if (trimmed == "False") return false
        if (trimmed == "None") return null
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) return trimmed.substring(1, trimmed.length - 1)
        if (trimmed.startsWith("'") && trimmed.endsWith("'")) return trimmed.substring(1, trimmed.length - 1)
        trimmed.toIntOrNull()?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }

        // List literal [1, 2, 3]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()
            return inner.split(",").map { evaluateExpression(it.trim(), env, functions) }.toMutableList()
        }

        // Function call: name(args...)
        val callParen = trimmed.indexOf("(")
        if (callParen > 0 && trimmed.endsWith(")")) {
            val fnName = trimmed.substring(0, callParen).trim()
            val argsRaw = trimmed.substring(callParen + 1, trimmed.length - 1)
            val fn = functions[fnName]
            if (fn != null) {
                val splitArgs = parseArguments(argsRaw)
                val evaluatedArgs = splitArgs.map { evaluateExpression(it, env, functions) }
                return fn(evaluatedArgs)
            }
        }

        // Dictionary lookup or List index: var[idx]
        val brOpen = trimmed.indexOf("[")
        if (brOpen > 0 && trimmed.endsWith("]")) {
            val targetVar = trimmed.substring(0, brOpen).trim()
            val keyExpr = trimmed.substring(brOpen + 1, trimmed.length - 1).trim()
            val target = env[targetVar]
            val keyVal = evaluateExpression(keyExpr, env, functions)

            if (target is List<*>) {
                val idx = (keyVal as? Number)?.toInt() ?: 0
                val actualIdx = if (idx < 0) target.size + idx else idx
                if (actualIdx in target.indices) return target[actualIdx]
            } else if (target is Map<*, *>) {
                return target[keyVal]
            }
        }

        // Arithmetic operators: + - * /
        for (op in listOf(" + ", " - ", " * ", " / ", " % ", " == ", " != ", " >= ", " <= ", " > ", " < ")) {
            val parts = trimmed.split(op, limit = 2)
            if (parts.size == 2) {
                val left = evaluateExpression(parts[0], env, functions)
                val right = evaluateExpression(parts[1], env, functions)
                return performBinaryOp(left, op.trim(), right)
            }
        }

        // Variable lookup
        if (env.containsKey(trimmed)) {
            return env[trimmed]
        }

        return trimmed
    }

    private fun performBinaryOp(left: Any?, op: String, right: Any?): Any? {
        if (left is Number && right is Number) {
            val dLeft = left.toDouble()
            val dRight = right.toDouble()
            return when (op) {
                "+" -> if (left is Int && right is Int) left + right else dLeft + dRight
                "-" -> if (left is Int && right is Int) left - right else dLeft - dRight
                "*" -> if (left is Int && right is Int) left * right else dLeft * dRight
                "/" -> if (dRight == 0.0) "Error: Division by zero" else if (dLeft % dRight == 0.0) (dLeft / dRight).toInt() else dLeft / dRight
                "%" -> (dLeft % dRight).toInt()
                "==" -> dLeft == dRight
                "!=" -> dLeft != dRight
                ">" -> dLeft > dRight
                "<" -> dLeft < dRight
                ">=" -> dLeft >= dRight
                "<=" -> dLeft <= dRight
                else -> null
            }
        }
        if (left is String || right is String) {
            if (op == "+") return "${left ?: ""}${right ?: ""}"
            if (op == "==") return left.toString() == right.toString()
            if (op == "!=") return left.toString() != right.toString()
        }
        return false
    }

    private fun parseArguments(raw: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()
        for (c in raw) {
            if (c == '(' || c == '[' || c == '{') depth++
            else if (c == ')' || c == ']' || c == '}') depth--

            if (c == ',' && depth == 0) {
                result.add(current.toString().trim())
                current = StringBuilder()
            } else {
                current.append(c)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString().trim())
        }
        return result.filter { it.isNotEmpty() }
    }

    private fun isValidIdentifier(name: String): Boolean {
        return name.all { it.isLetterOrDigit() || it == '_' } && !name.first().isDigit()
    }
}
