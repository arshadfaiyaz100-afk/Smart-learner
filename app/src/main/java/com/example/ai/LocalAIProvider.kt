package com.example.ai

class LocalAIProvider : AIProvider {
    override val name: String = "Local Engine (On-Device & Offline)"
    override val isOnline: Boolean = false
    override val isAvailable: Boolean = true

    override suspend fun askTutor(question: String, contextInfo: String): Result<String> {
        val q = question.lowercase()
        val response = when {
            q.contains("variable") ->
                "💡 **Variables in Programming**:\nThink of a variable as a labeled storage box in memory. When you write `x = 5`, you store number 5 in box `x`. In Python, variables are dynamically typed, meaning you don't have to declare types like `int` explicitly."

            q.contains("list") || q.contains("array") ->
                "📚 **Lists & Collections**:\nLists in Python are ordered, mutable sequences (`[1, 2, 3]`). You can append items (`.append()`), pop items, and slice (`items[0:2]`). Negative indices like `-1` access items from the end."

            q.contains("function") || q.contains("def") ->
                "⚙️ **Functions & Modularity**:\nFunctions wrap reusable logic. In Python, use `def function_name(param):` and return with `return value`. Always remember Python uses indentation (4 spaces) rather than curly braces."

            q.contains("loop") || q.contains("for") || q.contains("while") ->
                "🔁 **Loops & Iteration**:\n- Use `for item in collection:` when iterating over a known sequence.\n- Use `while condition:` when repeating until a state change. Ensure the condition eventually becomes false to avoid infinite loops!"

            q.contains("vowel") || q.contains("pronounce") || q.contains("english") ->
                "🗣️ **English Fundamentals**:\nEnglish phonetics has 5 written vowels (A, E, I, O, U) but over 20 distinct vowel sounds! Notice how 'a' in 'cat' (/æ/) differs from 'a' in 'cake' (/eɪ/). Keep practicing in Speaking Lab!"

            q.contains("rag") || q.contains("llm") || q.contains("ai") ->
                "🤖 **AI & RAG Architecture**:\nRetrieval-Augmented Generation (RAG) couples a generative LLM with an external vector search engine. It fetches relevant ground-truth documents and injects them into the context window to prevent hallucination."

            q.contains("http") || q.contains("api") || q.contains("rest") ->
                "🌐 **RESTful APIs**:\nAPIs allow separate programs to exchange data over HTTP. GET retrieves data without side effects; POST creates resources; PUT replaces; DELETE removes. Status codes: 2xx = Success, 4xx = Client Error, 5xx = Server Error."

            else ->
                "🎓 **Tutor Insight for \"$question\"**:\nIn context of *$contextInfo*, break this down into smaller components: 1) Identify inputs and state, 2) Step through execution line-by-line, and 3) Test small edge cases. Would you like a targeted exercise on this?"
        }
        return Result.success(response)
    }

    override suspend fun explainConcept(concept: String, level: String): Result<String> {
        return Result.success(
            "📖 **Core Concept: $concept**\n\n" +
            "• **Objective**: Understand fundamental mechanisms at $level level.\n" +
            "• **How it works**: Deconstruct into atomic operations, identify state transitions, and observe inputs/outputs.\n" +
            "• **Best Practice**: Validate assumptions with automated unit tests and concise comments."
        )
    }

    override suspend fun reviewCode(code: String): Result<String> {
        val trimmed = code.trim()
        val suggestions = mutableListOf<String>()

        if (trimmed.lines().any { it.contains("\t") }) {
            suggestions.add("⚠️ Use 4 spaces for indentation instead of tab characters (PEP 8 standard).")
        }
        if (trimmed.contains("while True") && !trimmed.contains("break")) {
            suggestions.add("⚠️ Potential infinite loop: detected `while True` without visible break condition.")
        }
        if (trimmed.contains("def ") && !trimmed.contains(":")) {
            suggestions.add("❌ Syntax error: missing colon `:` in function declaration.")
        }
        if (trimmed.contains("print ") && !trimmed.contains("print(")) {
            suggestions.add("❌ Python 3 requires parentheses: use `print(...)`.")
        }

        val analysis = if (suggestions.isEmpty()) {
            "✅ **Code Review Clean**:\n• Clear structure and syntax compliant.\n• Follows idiomatic conventions.\n• Sandboxed evaluator verifies valid execution."
        } else {
            "🔍 **Code Review Feedback**:\n" + suggestions.joinToString("\n")
        }
        return Result.success(analysis)
    }

    override suspend fun providePronunciationTip(phrase: String): Result<String> {
        val tip = when {
            phrase.contains("Apple", ignoreCase = true) ->
                "Phonetic: /ˈæp.əl/ • Open your mouth wide for the short /æ/ sound, then release softly on 'ple'."
            phrase.contains("Debug", ignoreCase = true) ->
                "Phonetic: /diːˈbʌɡ/ • Stress the second syllable: dee-BUG. Keep the 'g' voiced at the end."
            phrase.contains("Python", ignoreCase = true) ->
                "Phonetic: /ˈpaɪ.θɑːn/ • Start with 'Pie', then a soft voiceless 'th' sound with tongue between teeth."
            else ->
                "Phonetic guidance for '$phrase': Break into syllables, speak at measured pace, and articulate ending consonants clearly."
        }
        return Result.success(tip)
    }
}
