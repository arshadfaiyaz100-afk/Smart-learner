package com.example.curriculum

import com.example.data.model.CourseUnit
import com.example.data.model.Exercise
import com.example.data.model.ExerciseOption
import com.example.data.model.ExerciseType
import com.example.data.model.LearningTrack
import com.example.data.model.Lesson
import com.example.data.model.ProjectItem
import com.example.data.model.TestCase

object CurriculumRegistry {

    val tracks = LearningTrack.values().sortedBy { it.orderIndex }

    val units: List<CourseUnit> by lazy {
        listOf(
            // ----------------------------------------------------
            // 1. ABSOLUTE ZERO ENGLISH
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_en_zero_1",
                track = LearningTrack.ZERO_ENGLISH,
                title = "From Absolute Zero: Letters & Sounds",
                description = "Start with zero knowledge: discover letter shapes, basic phonics, and everyday words.",
                orderIndex = 1,
                lessons = listOf(
                    Lesson(
                        id = "lesson_welcome_00",
                        unitId = "unit_en_zero_1",
                        title = "Welcome: How This App Works",
                        track = LearningTrack.ZERO_ENGLISH,
                        objective = "Learn how to navigate lessons, tap answers, and track your daily learning.",
                        teachContent = "Welcome to Learning Lab! You do not need any prior knowledge. Every lesson shows a simple idea, then asks you to tap the correct choice. As you answer correctly, you earn XP and unlock new abilities.",
                        teachContentHinglish = "Learning Lab me aapka swagat hai! Yahan aapko pehle se kuch aana zaroori nahi hai. Har lesson me ek aasan baat sikhayi jayegi, fir aapko sahi option tap karna hoga. Sahi answer par XP milega aur nayi capabilities unlock hongi.",
                        codeExample = "Tap Option -> Check Answer -> Earn XP -> Unlock Next",
                        xpReward = 15,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_welcome_1",
                                lessonId = "lesson_welcome_00",
                                type = ExerciseType.MCQ,
                                prompt = "Tap the button that says 'I am ready to learn!':",
                                options = listOf(
                                    ExerciseOption("1", "I am ready to learn!"),
                                    ExerciseOption("2", "Not now"),
                                    ExerciseOption("3", "Close app")
                                ),
                                correctIndex = 0,
                                explanation = "Great job! Tapping an option is how you answer questions in Learning Lab.",
                                explanationHinglish = "Shabash! Isi tarah se option tap karke aap questions ka answer dete hain.",
                                hint = "Tap the first option."
                            ),
                            Exercise(
                                id = "ex_welcome_2",
                                lessonId = "lesson_welcome_00",
                                type = ExerciseType.TRUE_FALSE,
                                prompt = "True or False: You can start learning here even if you know zero English or programming.",
                                options = listOf(
                                    ExerciseOption("1", "True"),
                                    ExerciseOption("2", "False")
                                ),
                                correctIndex = 0,
                                explanation = "Yes! Learning Lab assumes knowledge = 0 and builds every concept step by step.",
                                explanationHinglish = "Haan bilkul! Yeh app zero se shuru karke aapko hero level tak le jata hai.",
                                hint = "Choose True."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_en_01",
                        unitId = "unit_en_zero_1",
                        title = "The Letter 'A' & Letter Recognition",
                        track = LearningTrack.ZERO_ENGLISH,
                        objective = "Recognize capital 'A', small 'a', and the primary vowel sound /æ/.",
                        teachContent = "English writing begins with letters. The very first letter is 'A'. Capital: A, Small: a. The letter 'A' makes the sound /æ/ as in 'Apple'.",
                        teachContentHinglish = "English ki shuruat letters se hoti hai. Sabse pehla letter hai 'A'. Bada 'A' aur chota 'a'. Iski aawaz hoti hai /æ/ jaise 'Apple' me.",
                        codeExample = "Letter: A / a\nSound: /æ/ (as in Apple)",
                        xpReward = 20,
                        orderIndex = 2,
                        exercises = listOf(
                            Exercise(
                                id = "ex_en_01_1",
                                lessonId = "lesson_en_01",
                                type = ExerciseType.MCQ,
                                prompt = "Which letter is the first letter of the English alphabet?",
                                options = listOf(
                                    ExerciseOption("1", "B"),
                                    ExerciseOption("2", "A"),
                                    ExerciseOption("3", "Z"),
                                    ExerciseOption("4", "M")
                                ),
                                correctIndex = 1,
                                explanation = "'A' is the first letter in the English alphabet.",
                                explanationHinglish = "English alphabet ka sabse pehla letter 'A' hota hai.",
                                hint = "Look for the letter shaped like a triangle with legs."
                            ),
                            Exercise(
                                id = "ex_en_01_2",
                                lessonId = "lesson_en_01",
                                type = ExerciseType.FILL_BLANKS,
                                prompt = "Type the missing letter: _ pple",
                                correctText = "A",
                                explanation = "A-P-P-L-E spells Apple.",
                                explanationHinglish = "A lagane se 'Apple' banta hai.",
                                hint = "The letter A."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_en_02",
                        unitId = "unit_en_zero_1",
                        title = "Letters B, C, D & The Word 'CAT'",
                        track = LearningTrack.ZERO_ENGLISH,
                        objective = "Combine consonants and vowels to form your first complete 3-letter word.",
                        teachContent = "When letters join together, they create words! 'C' makes /k/, 'A' makes /æ/, and 'T' makes /t/. Joining them: C + A + T = CAT. A cat is a friendly animal.",
                        teachContentHinglish = "Jab letters milte hain toh shabd (word) banta hai! C + A + T milkar 'CAT' banta hai. Cat matlab billi.",
                        codeExample = "C [k] + A [æ] + T [t] = CAT",
                        xpReward = 20,
                        orderIndex = 3,
                        exercises = listOf(
                            Exercise(
                                id = "ex_en_02_1",
                                lessonId = "lesson_en_02",
                                type = ExerciseType.MCQ,
                                prompt = "What word is formed by combining C + A + T?",
                                options = listOf(
                                    ExerciseOption("1", "DOG"),
                                    ExerciseOption("2", "CAT"),
                                    ExerciseOption("3", "BAT")
                                ),
                                correctIndex = 1,
                                explanation = "C + A + T = CAT.",
                                explanationHinglish = "C-A-T milkar 'CAT' banta hai.",
                                hint = "Look at the letters C, A, T."
                            ),
                            Exercise(
                                id = "ex_en_02_2",
                                lessonId = "lesson_en_02",
                                type = ExerciseType.SPEAKING,
                                prompt = "Speak the word clearly:",
                                targetPhrase = "Cat",
                                explanation = "Articulate the ending 't' crisp and clean.",
                                hint = "Pronounce: KAT"
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_en_03",
                        unitId = "unit_en_zero_1",
                        title = "Essential Everyday & Tech Words",
                        track = LearningTrack.ZERO_ENGLISH,
                        objective = "Learn everyday nouns and primary digital interface terms: Screen, Click, Book, Water.",
                        teachContent = "Nouns name things. In our daily and digital life: 'Book' is for reading. 'Water' is for drinking. 'Screen' is the glass surface you look at. 'Click' or 'Tap' means pressing on the screen.",
                        teachContentHinglish = "Cheezon ke naam ko noun bolte hain. 'Screen' wo hissa hai jahan aap dekh rahe hain. 'Tap' ya 'Click' matlab screen par ungli dabana.",
                        codeExample = "Everyday: Book, Water\nDigital: Screen, Click, Tap",
                        xpReward = 25,
                        orderIndex = 4,
                        exercises = listOf(
                            Exercise(
                                id = "ex_en_03_1",
                                lessonId = "lesson_en_03",
                                type = ExerciseType.MCQ,
                                prompt = "What do we call the display area of your phone or computer?",
                                options = listOf(
                                    ExerciseOption("1", "Screen"),
                                    ExerciseOption("2", "Water"),
                                    ExerciseOption("3", "Cat")
                                ),
                                correctIndex = 0,
                                explanation = "The display surface where pictures and text appear is the 'Screen'.",
                                explanationHinglish = "Display wale hisse ko 'Screen' kaha jata hai.",
                                hint = "It begins with S."
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 2. MATHEMATICS FROM ZERO
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_math_zero_1",
                track = LearningTrack.MATH_ZERO,
                title = "Numbers, Counting & Basic Calculations",
                description = "Learn numbers, counting items, comparing amounts, and fundamental addition.",
                orderIndex = 2,
                lessons = listOf(
                    Lesson(
                        id = "lesson_math_01",
                        unitId = "unit_math_zero_1",
                        title = "Numbers & Counting from Zero",
                        track = LearningTrack.MATH_ZERO,
                        objective = "Count quantities from 0 to 5 and identify their numeric symbols.",
                        teachContent = "0 means nothing or empty. 1 is one single item (●). 2 is two items (●●). 3 is three items (●●●). Computers use numbers for all logic.",
                        teachContentHinglish = "0 ka matlab hota hai kuch nahi (empty). 1 matlab ek cheez. 2 matlab do cheezein. Computer saara kaam numbers se hi karta hai.",
                        codeExample = "0: Empty\n1: ●\n2: ●●\n3: ●●●",
                        xpReward = 20,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_math_01_1",
                                lessonId = "lesson_math_01",
                                type = ExerciseType.MCQ,
                                prompt = "How many dots are here: ● ● ●",
                                options = listOf(
                                    ExerciseOption("1", "1"),
                                    ExerciseOption("2", "2"),
                                    ExerciseOption("3", "3"),
                                    ExerciseOption("4", "4")
                                ),
                                correctIndex = 2,
                                explanation = "Counting them: one, two, three (3).",
                                hint = "Count each dot."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_math_02",
                        unitId = "unit_math_zero_1",
                        title = "Comparison & Equality",
                        track = LearningTrack.MATH_ZERO,
                        objective = "Understand greater than (>), less than (<), and equal to (=).",
                        teachContent = "Numbers can be compared! '=' means equal (same amount). '>' means greater (more). '<' means less (fewer). For example: 5 > 2 (5 is greater than 2).",
                        teachContentHinglish = "'=' ka matlab barabar. '>' ka matlab bada. '<' ka matlab chota. 5 bada hai 2 se: 5 > 2.",
                        codeExample = "Equal: 3 = 3\nGreater: 5 > 2\nLess: 1 < 4",
                        xpReward = 25,
                        orderIndex = 2,
                        exercises = listOf(
                            Exercise(
                                id = "ex_math_02_1",
                                lessonId = "lesson_math_02",
                                type = ExerciseType.TRUE_FALSE,
                                prompt = "Is 4 greater than 2? (4 > 2)",
                                options = listOf(
                                    ExerciseOption("1", "True"),
                                    ExerciseOption("2", "False")
                                ),
                                correctIndex = 0,
                                explanation = "Yes, 4 represents a larger quantity than 2.",
                                hint = "4 is more than 2."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_math_03",
                        unitId = "unit_math_zero_1",
                        title = "Basic Addition: Joining Amounts",
                        track = LearningTrack.MATH_ZERO,
                        objective = "Add two numbers together using the plus (+) operator.",
                        teachContent = "Addition means joining groups together. If you have 2 apples and receive 2 more apples, you now have 4 apples! We write: 2 + 2 = 4.",
                        teachContentHinglish = "Addition (+) ka matlab jodna. 2 me 2 jodenge toh 4 banega: 2 + 2 = 4.",
                        codeExample = "2 + 2 = 4\n3 + 1 = 4",
                        xpReward = 25,
                        orderIndex = 3,
                        exercises = listOf(
                            Exercise(
                                id = "ex_math_03_1",
                                lessonId = "lesson_math_03",
                                type = ExerciseType.MCQ,
                                prompt = "What is 2 + 3?",
                                options = listOf(
                                    ExerciseOption("1", "4"),
                                    ExerciseOption("2", "5"),
                                    ExerciseOption("3", "6")
                                ),
                                correctIndex = 1,
                                explanation = "2 + 3 = 5.",
                                hint = "Count 2, then add 3 more: 3, 4, 5."
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 3. COMPUTER FUNDAMENTALS FROM ZERO
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_comp_zero_1",
                track = LearningTrack.COMPUTER_LITERACY,
                title = "Understanding the Computer",
                description = "Learn how hardware, files, folders, and operating systems function before writing code.",
                orderIndex = 3,
                lessons = listOf(
                    Lesson(
                        id = "lesson_comp_01",
                        unitId = "unit_comp_zero_1",
                        title = "What is a Computer?",
                        track = LearningTrack.COMPUTER_LITERACY,
                        objective = "Differentiate between Screen (output), Keyboard/Touch (input), and Processor (brain).",
                        teachContent = "A computer is an electronic machine that takes INPUT (from keyboard or touch), PROCESSES it with its CPU (the brain), and produces OUTPUT (on the screen).",
                        teachContentHinglish = "Computer ek aisi machine hai jo input leti hai (keyboard ya touch se), processor us par kaam karta hai, aur screen par output dikhata hai.",
                        codeExample = "Input (Touch/Keys) -> Processing (CPU) -> Output (Screen)",
                        xpReward = 25,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_comp_01_1",
                                lessonId = "lesson_comp_01",
                                type = ExerciseType.MCQ,
                                prompt = "Which part of the computer acts as the calculating 'brain'?",
                                options = listOf(
                                    ExerciseOption("1", "The Screen"),
                                    ExerciseOption("2", "The CPU (Processor)"),
                                    ExerciseOption("3", "The Mouse wire")
                                ),
                                correctIndex = 1,
                                explanation = "The Central Processing Unit (CPU) executes all calculations.",
                                hint = "CPU stands for Central Processing Unit."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_comp_02",
                        unitId = "unit_comp_zero_1",
                        title = "Files, Folders & Extensions",
                        track = LearningTrack.COMPUTER_LITERACY,
                        objective = "Understand how data is stored in files and grouped in folders.",
                        teachContent = "A 'File' stores information (like text or a picture). A 'Folder' is a container that organizes multiple files. A file name has an extension: `.txt` for text, `.py` for Python programs, `.png` for images.",
                        teachContentHinglish = "Information ko 'File' me rakha jata hai. Bahut saari files ko sametne ke liye 'Folder' hota hai. File ke naam ke aage extension hota hai jaise `.txt` ya `.py`.",
                        codeExample = "notes.txt  -> Text file\nscript.py  -> Python file\nphoto.png  -> Image file",
                        xpReward = 30,
                        orderIndex = 2,
                        exercises = listOf(
                            Exercise(
                                id = "ex_comp_02_1",
                                lessonId = "lesson_comp_02",
                                type = ExerciseType.MCQ,
                                prompt = "What file extension indicates a Python program file?",
                                options = listOf(
                                    ExerciseOption("1", ".txt"),
                                    ExerciseOption("2", ".py"),
                                    ExerciseOption("3", ".mp3")
                                ),
                                correctIndex = 1,
                                explanation = "Files ending in `.py` contain Python programming code.",
                                hint = "Short for Python: .py"
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 4. PROGRAMMING LOGIC (BEFORE CODE)
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_logic_1",
                track = LearningTrack.PROGRAMMING_LOGIC,
                title = "Thinking Like a Programmer",
                description = "Master sequences, conditions, loops, and variables using plain visual logic before syntax.",
                orderIndex = 4,
                lessons = listOf(
                    Lesson(
                        id = "lesson_logic_01",
                        unitId = "unit_logic_1",
                        title = "What is a Program?",
                        track = LearningTrack.PROGRAMMING_LOGIC,
                        objective = "Understand that a program is simply an ordered recipe of instructions.",
                        teachContent = "A program is like a cooking recipe. Step 1: Open door. Step 2: Walk inside. Step 3: Turn on light. Computers follow each instruction one by one from top to bottom.",
                        teachContentHinglish = "Program ek recipe jaisa hota hai: ek ke baad ek steps. Computer upar se neeche tak har instruction ko line-by-line follow karta hai.",
                        codeExample = "Step 1: Wake up\nStep 2: Brush teeth\nStep 3: Eat breakfast",
                        xpReward = 30,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_logic_01_1",
                                lessonId = "lesson_logic_01",
                                type = ExerciseType.MCQ,
                                prompt = "In what order does a computer normally execute instructions?",
                                options = listOf(
                                    ExerciseOption("1", "Random order"),
                                    ExerciseOption("2", "From top to bottom, one by one"),
                                    ExerciseOption("3", "From bottom to top backwards")
                                ),
                                correctIndex = 1,
                                explanation = "Programs run sequentially in the exact order written.",
                                hint = "Top to bottom."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_logic_02",
                        unitId = "unit_logic_1",
                        title = "Variables: The Labeled Memory Box",
                        track = LearningTrack.PROGRAMMING_LOGIC,
                        objective = "Understand variables as labeled storage spaces for data.",
                        teachContent = "A program needs a place to keep information while running. That place is called a VARIABLE. Imagine a box with a label 'player_name' containing the text 'Arshad'. When you ask for 'player_name', you get 'Arshad'.",
                        teachContentHinglish = "Program ko information yaad rakhne ke liye ek jagah chahiye hoti hai, jise VARIABLE bolte hain. Jaise ek dabba jiske upar naam likha ho 'score' aur andar number ho 10.",
                        codeExample = "Box Name: player_name\nInside: \"Arshad\"\nBox Name: score\nInside: 100",
                        xpReward = 35,
                        orderIndex = 2,
                        exercises = listOf(
                            Exercise(
                                id = "ex_logic_02_1",
                                lessonId = "lesson_logic_02",
                                type = ExerciseType.MCQ,
                                prompt = "What is the purpose of a variable in a computer program?",
                                options = listOf(
                                    ExerciseOption("1", "To turn off the computer"),
                                    ExerciseOption("2", "To store and label information in memory"),
                                    ExerciseOption("3", "To break the screen")
                                ),
                                correctIndex = 1,
                                explanation = "Variables hold and label values so the program can use them later.",
                                hint = "Think of a labeled storage container."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_logic_03",
                        unitId = "unit_logic_1",
                        title = "Conditions: Making Decisions",
                        track = LearningTrack.PROGRAMMING_LOGIC,
                        objective = "Understand IF and ELSE branching decisions.",
                        teachContent = "Real life has decisions: IF it is raining, take an umbrella. ELSE, wear sunglasses. A program uses IF/ELSE to choose different paths based on conditions.",
                        teachContentHinglish = "Zindagi me hum decisions lete hain: AGAR (IF) barish ho rahi hai toh chata lo, WARNA (ELSE) dhoop ka chashma. Computer bhi aise hi faisle leta hai.",
                        codeExample = "IF score >= 50:\n    Pass\nELSE:\n    Retry",
                        xpReward = 35,
                        orderIndex = 3,
                        exercises = listOf(
                            Exercise(
                                id = "ex_logic_03_1",
                                lessonId = "lesson_logic_03",
                                type = ExerciseType.MCQ,
                                prompt = "If `score = 80`, and the rule is `IF score >= 50: Pass ELSE: Retry`, what is the result?",
                                options = listOf(
                                    ExerciseOption("1", "Pass"),
                                    ExerciseOption("2", "Retry")
                                ),
                                correctIndex = 0,
                                explanation = "Since 80 is greater than 50, the condition is met and result is 'Pass'.",
                                hint = "80 is greater than 50."
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 5. PYTHON FOUNDATION & CORE
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_py_core_1",
                track = LearningTrack.PYTHON_MASTERY,
                title = "Python from Zero: First Program & Syntax",
                description = "Learn Python syntax step-by-step: print(), variables, numbers, strings, and custom functions.",
                orderIndex = 5,
                lessons = listOf(
                    Lesson(
                        id = "lesson_py_01",
                        unitId = "unit_py_core_1",
                        title = "What is Python & print('Hello')",
                        track = LearningTrack.PYTHON_MASTERY,
                        objective = "Deconstruct the first Python command print('Hello') piece by piece.",
                        teachContent = "Python is a programming language that lets you talk to the computer using plain English-like words. To make the computer show text on screen, we write: print(\"Hello\"). 'print' means display, '(' opens instructions, '\"Hello\"' is the text, and ')' closes instructions.",
                        teachContentHinglish = "Python ek aasan programming language hai. Screen par kuch likhne ke liye hum likhte hain: print(\"Hello\"). Yahan 'print' ka matlab screen par dikhana, aur quotes ke andar message hota hai.",
                        codeExample = "print(\"Hello\")\n# Displays: Hello",
                        xpReward = 35,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_py_01_1",
                                lessonId = "lesson_py_01",
                                type = ExerciseType.MCQ,
                                prompt = "Which command displays text on the screen in Python?",
                                options = listOf(
                                    ExerciseOption("1", "show[]"),
                                    ExerciseOption("2", "print(\"text\")"),
                                    ExerciseOption("3", "speak<>")
                                ),
                                correctIndex = 1,
                                explanation = "`print(...)` is Python's built-in function to output text.",
                                hint = "The word 'print' followed by parentheses."
                            ),
                            Exercise(
                                id = "ex_py_01_2",
                                lessonId = "lesson_py_01",
                                type = ExerciseType.CODE_PREDICTION,
                                prompt = "What does this code output?\nprint(\"Python is fun\")",
                                options = listOf(
                                    ExerciseOption("1", "Python is fun"),
                                    ExerciseOption("2", "print"),
                                    ExerciseOption("3", "Error")
                                ),
                                correctIndex = 0,
                                explanation = "It outputs the string text inside the quotes.",
                                hint = "The text inside the quotation marks."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_py_02",
                        unitId = "unit_py_core_1",
                        title = "Python Variables & Calculations",
                        track = LearningTrack.PYTHON_MASTERY,
                        objective = "Assign numbers to variables and perform calculations in Python.",
                        teachContent = "In Python, creating a variable is as simple as writing `x = 5`. The `=` sign stores value 5 into `x`. You can then do math: `y = x + 3` results in `y = 8`.",
                        teachContentHinglish = "Python me variable banana bohot aasan hai: `x = 5`. '=' ka matlab hai 5 ko 'x' me store karo. `y = x + 3` karne se y ki value 8 ho jayegi.",
                        codeExample = "x = 5\ny = x + 3\nprint(y) # Outputs 8",
                        xpReward = 40,
                        orderIndex = 2,
                        exercises = listOf(
                            Exercise(
                                id = "ex_py_02_1",
                                lessonId = "lesson_py_02",
                                type = ExerciseType.CODE_PREDICTION,
                                prompt = "What is the output of:\na = 10\nb = 4\nprint(a - b)",
                                options = listOf(
                                    ExerciseOption("1", "14"),
                                    ExerciseOption("2", "6"),
                                    ExerciseOption("3", "40")
                                ),
                                correctIndex = 1,
                                explanation = "10 - 4 = 6.",
                                hint = "10 minus 4."
                            )
                        )
                    ),
                    Lesson(
                        id = "lesson_py_03",
                        unitId = "unit_py_core_1",
                        title = "Python Functions & Reusable Code",
                        track = LearningTrack.PYTHON_MASTERY,
                        objective = "Create your own custom function using `def` and `return`.",
                        teachContent = "A function is a reusable mini-program. Use the `def` keyword: `def double(n): return n * 2`. Then whenever you call `double(5)`, it returns `10`.",
                        teachContentHinglish = "Function ek reusable code block hota hai. `def` keyword se banta hai. Jab bhi call karenge, ye apna kaam karke result wapas (return) karega.",
                        codeExample = "def double(n):\n    return n * 2\n\nprint(double(7)) # 14",
                        xpReward = 45,
                        orderIndex = 3,
                        exercises = listOf(
                            Exercise(
                                id = "ex_py_03_1",
                                lessonId = "lesson_py_03",
                                type = ExerciseType.CODE_PREDICTION,
                                prompt = "What does `double(4)` return given `def double(n): return n * 2`?",
                                options = listOf(
                                    ExerciseOption("1", "6"),
                                    ExerciseOption("2", "8"),
                                    ExerciseOption("3", "16")
                                ),
                                correctIndex = 1,
                                explanation = "4 multiplied by 2 is 8.",
                                hint = "4 * 2."
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 6. SOFTWARE ENGINEERING & TOOLS
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_se_1",
                track = LearningTrack.SOFTWARE_ENGINEERING,
                title = "APIs, Git & Production Tools",
                description = "HTTP protocols, REST APIs, Git version control, and automated software testing.",
                orderIndex = 6,
                lessons = listOf(
                    Lesson(
                        id = "lesson_se_01",
                        unitId = "unit_se_1",
                        title = "HTTP Methods & Status Codes",
                        track = LearningTrack.SOFTWARE_ENGINEERING,
                        objective = "Understand GET, POST, 200 OK, and 404 Not Found in web communications.",
                        teachContent = "APIs allow different apps to talk over the web using HTTP. GET retrieves data without changing anything. POST creates new data. Status code 200 means Success, 404 means Not Found.",
                        teachContentHinglish = "APIs ke zariye apps aapas me baat karte hain. GET data mangwata hai, POST naya data bhejta hai. Status code 200 matlab sab theek hai (OK), 404 matlab cheez nahi mili.",
                        codeExample = "GET /users -> 200 OK\nPOST /users -> 201 Created",
                        xpReward = 45,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_se_01_1",
                                lessonId = "lesson_se_01",
                                type = ExerciseType.MCQ,
                                prompt = "Which HTTP status code signifies that a request was successful?",
                                options = listOf(
                                    ExerciseOption("1", "404"),
                                    ExerciseOption("2", "500"),
                                    ExerciseOption("3", "200")
                                ),
                                correctIndex = 2,
                                explanation = "200 OK is the standard HTTP success status code.",
                                hint = "200 OK."
                            )
                        )
                    )
                )
            ),

            // ----------------------------------------------------
            // 7. AI & INDUSTRY PROJECTS
            // ----------------------------------------------------
            CourseUnit(
                id = "unit_ai_1",
                track = LearningTrack.AI_ENGINEERING,
                title = "AI Engineering & Real Projects",
                description = "LLMs, tokens, prompt engineering, RAG architecture, and production portfolio projects.",
                orderIndex = 7,
                lessons = listOf(
                    Lesson(
                        id = "lesson_ai_01",
                        unitId = "unit_ai_1",
                        title = "Prompts, Tokens & Context Windows",
                        track = LearningTrack.AI_ENGINEERING,
                        objective = "Understand how AI models break text into tokens and reason through prompts.",
                        teachContent = "Large Language Models (LLMs) break words into pieces called 'tokens' (~4 characters). A prompt is the text instruction you give to the AI. The context window is how many tokens the AI can remember at once.",
                        teachContentHinglish = "AI models shabdon ko 'tokens' me todkar samajhte hain. Jo instruction hum dete hain use 'prompt' bolte hain. Context window matlab AI ek baar me kitna yaad rakh sakta hai.",
                        codeExample = "User Prompt -> Tokenizer -> Transformer -> Prediction",
                        xpReward = 50,
                        orderIndex = 1,
                        exercises = listOf(
                            Exercise(
                                id = "ex_ai_01_1",
                                lessonId = "lesson_ai_01",
                                type = ExerciseType.MCQ,
                                prompt = "What do we call the text instruction sent to an AI model?",
                                options = listOf(
                                    ExerciseOption("1", "A Prompt"),
                                    ExerciseOption("2", "A Cable"),
                                    ExerciseOption("3", "A Hardware chip")
                                ),
                                correctIndex = 0,
                                explanation = "The instruction guiding an AI model's generation is called a Prompt.",
                                hint = "Prompt."
                            )
                        )
                    )
                )
            )
        )
    }

    val projects: List<ProjectItem> = listOf(
        ProjectItem(
            id = "proj_01_calc",
            title = "CLI Safe Scientific Calculator",
            track = LearningTrack.PYTHON_MASTERY,
            level = "Beginner",
            description = "Build a robust calculator supporting addition, subtraction, multiplication, division, and error-handling for division by zero.",
            architecture = "Input Parser -> Token Validator -> Arithmetic Engine -> Formatted Output",
            starterCode = """
# Task: Complete the calculator function
def calculate(a, op, b):
    if op == '+':
        return a + b
    elif op == '-':
        return a - b
    elif op == '*':
        return a * b
    elif op == '/':
        if b == 0:
            return "Error: Division by zero"
        return a / b
    return "Error: Unknown operator"

print("5 + 3 =", calculate(5, '+', 3))
print("10 / 2 =", calculate(10, '/', 2))
print("4 / 0 =", calculate(4, '/', 0))
""".trimIndent(),
            solutionCode = """
def calculate(a, op, b):
    if op == '+': return a + b
    if op == '-': return a - b
    if op == '*': return a * b
    if op == '/':
        return "Error: Division by zero" if b == 0 else a / b
    return "Error: Unknown operator"
""".trimIndent(),
            testCases = listOf(
                TestCase("Add", "calculate(5, '+', 3)", "8"),
                TestCase("Div by Zero", "calculate(4, '/', 0)", "Error: Division by zero")
            ),
            xpReward = 100
        ),
        ProjectItem(
            id = "proj_02_quiz",
            title = "Interactive Adaptive Quiz Engine",
            track = LearningTrack.PYTHON_MASTERY,
            level = "Intermediate",
            description = "Create an automated question-and-answer scoring engine that tracks streaks and calculates final letter grades.",
            architecture = "Question Repository -> User Input Loop -> Score Accumulator -> Analytics Summary",
            starterCode = """
# Task: Create a function that evaluates a quiz submission
def evaluate_quiz(answers, answer_key):
    score = 0
    total = len(answer_key)
    for q_id, correct_ans in answer_key.items():
        if answers.get(q_id) == correct_ans:
            score += 1
    percentage = (score / total) * 100
    return {"score": score, "total": total, "percent": percentage}

key = {"q1": "B", "q2": "A", "q3": "C"}
user_sub = {"q1": "B", "q2": "D", "q3": "C"}
print(evaluate_quiz(user_sub, key))
""".trimIndent(),
            solutionCode = "",
            testCases = listOf(
                TestCase("Accuracy test", "evaluate_quiz({'q1':'B'}, {'q1':'B'})", "{'score': 1, 'total': 1, 'percent': 100.0}")
            ),
            xpReward = 120
        ),
        ProjectItem(
            id = "proj_03_rag",
            title = "Local Semantic Search & RAG Simulator",
            track = LearningTrack.AI_ENGINEERING,
            level = "Advanced",
            description = "Simulate a vector embedding search that finds the closest documentation chunk and crafts a contextual prompt for an LLM.",
            architecture = "Document Chunker -> Keyword/Vector Scorer -> Top-K Selector -> Prompt Synthesizer",
            starterCode = """
# RAG Context Matcher
def find_relevant_doc(query, knowledge_base):
    best_doc = None
    max_matches = 0
    query_words = set(query.lower().split())
    
    for doc in knowledge_base:
        doc_words = set(doc.lower().split())
        overlap = len(query_words.intersection(doc_words))
        if overlap > max_matches:
            max_matches = overlap
            best_doc = doc
            
    return best_doc or "No matching documentation found."

docs = [
    "Python lists are ordered, mutable sequences defined with square brackets.",
    "Git is a distributed version control system tracking source code history.",
    "RAG retrieves relevant external documents before passing prompts to an LLM."
]

print(find_relevant_doc("How do python lists work?", docs))
""".trimIndent(),
            solutionCode = "",
            testCases = listOf(
                TestCase("Match Python", "find_relevant_doc('python lists', docs)", "Python lists are ordered...")
            ),
            xpReward = 150
        )
    )

    fun getAllLessons(): List<Lesson> = units.flatMap { it.lessons }

    fun getLessonById(id: String): Lesson? = getAllLessons().find { it.id == id }

    fun getNextLesson(currentLessonId: String): Lesson? {
        val all = getAllLessons()
        val index = all.indexOfFirst { it.id == currentLessonId }
        return if (index >= 0 && index < all.size - 1) all[index + 1] else null
    }

    fun getFirstLesson(): Lesson = getAllLessons().first()
}
