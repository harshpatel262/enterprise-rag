# VHS script that records docs/demo.gif — https://github.com/charmbracelet/vhs
# Prereq: app running on :8080 (zero-infra profile)
#   mvn spring-boot:run
# Record: vhs docs/demo.tape

Output docs/demo.gif

Set FontSize 14
Set Width 1150
Set Height 640
Set Padding 16
Set TypingSpeed 25ms
Set Theme "Catppuccin Mocha"

Type "# 1. Ingest documents -> chunked, embedded, indexed"
Enter
Sleep 800ms
Type `curl -s localhost:8080/api/documents -H 'Content-Type: application/json' -d '{"title": "Expense Policy", "content": "Reimbursable expenses must be submitted within 60 days. Meals are capped at $75 per day during travel. Receipts are required above $25."}' | jq`
Enter
Sleep 3s
Type `curl -s localhost:8080/api/documents -H 'Content-Type: application/json' -d '{"title": "Onboarding Guide", "content": "New hires receive laptops on day one and complete security training during their first week."}' | jq -c '{documentId, chunkCount}'`
Enter
Sleep 3s

Type "# 2. Ask a question -> vector retrieval + citation-grounded answer"
Enter
Sleep 800ms
Type `curl -s localhost:8080/api/query -H 'Content-Type: application/json' -d '{"question": "What is the daily meal cap when traveling?", "topK": 2}' | jq '{answer, retrievalMs, citations: [.citations[] | {documentTitle, score}]}'`
Enter
Sleep 6s
