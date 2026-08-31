package com.slayer.backend.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.dto.ChatbotRequest;
import com.slayer.backend.dto.ChatbotResponse;
import com.slayer.backend.entity.Book;
import com.slayer.backend.entity.BookLoan;
import com.slayer.backend.entity.User;
import com.slayer.backend.repository.BookLoanRepository;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ChatbotResponse processChat(ChatbotRequest request, User user) {
        String userMessage = request.getMessage();

        // Step 1: Detect Intent
        String intentPrompt = "Extract intent from the user message. " +
                "Possible intents: CHECK_BOOK (check book availability), LOCATE_BOOK (finding a book's shelf/genre/location), MY_LOANS (asking about their currently borrowed books), MY_HISTORY (asking how many books they have read/returned in the past), TOP_BOOKS (asking for highest rated, most popular, or top books), GENERAL (anything else). " +
                "Return pure JSON (no markdown block) with fields: 'intent' (string) and 'keyword' (string, the book title if applicable, else null). " +
                "Message: " + userMessage;

        String intentJsonResp = callGemini(intentPrompt, true);
        
        String intent = "GENERAL";
        String keyword = null;
        try {
            JsonObject jsonObj = JsonParser.parseString(intentJsonResp).getAsJsonObject();
            if (jsonObj.has("intent") && !jsonObj.get("intent").isJsonNull()) {
                intent = jsonObj.get("intent").getAsString();
            }
            if (jsonObj.has("keyword") && !jsonObj.get("keyword").isJsonNull()) {
                keyword = jsonObj.get("keyword").getAsString();
            }
        } catch (Exception e) {
            log.error("Failed to parse intent JSON: {}", intentJsonResp, e);
        }

        // Step 2: Fetch Data context based on intent
        String databaseContext = "No database records retrieved.";
        
        switch (intent) {
            case "CHECK_BOOK":
                if (keyword != null && !keyword.isBlank()) {
                    Page<Book> books = bookRepository.searchBooksWithFilters(keyword, null, false, true, PageRequest.of(0, 5));
                    if(books.isEmpty()) {
                        Page<Book> randomBooks = bookRepository.searchBooksWithFilters(null, null, false, true, PageRequest.of(0, 5));
                        databaseContext = "No books exactly matched '" + keyword + "', but here are some other popular/latest books in the system:\n" + formatBooks(randomBooks);
                    } else {
                        databaseContext = "Found books matching '" + keyword + "':\n" + formatBooks(books);
                    }
                } else {
                    Page<Book> generalBooks = bookRepository.searchBooksWithFilters(null, null, false, true, PageRequest.of(0, 5));
                    databaseContext = "No specific keyword provided. Here are some featured books in the system:\n" + formatBooks(generalBooks);
                }
                break;
            case "LOCATE_BOOK":
                if (keyword != null && !keyword.isBlank()) {
                    Page<Book> books = bookRepository.searchBooksWithFilters(keyword, null, false, true, PageRequest.of(0, 5));
                    if(books.isEmpty()) {
                        databaseContext = "No books found matching '" + keyword + "'.";
                    } else {
                        StringBuilder sb = new StringBuilder("Location of books matching '" + keyword + "':\n");
                        for(Book b : books.getContent()) {
                            String genreName = (b.getGenre() != null) ? b.getGenre().getName() : "General";
                            sb.append("- ").append(b.getTitle()).append(" is located in the '")
                              .append(genreName).append("' genre/shelf.\n");
                        }
                        databaseContext = sb.toString();
                    }
                } else {
                    databaseContext = "Keyword missing for locating book.";
                }
                break;
            case "MY_LOANS":
                if (user != null) {
                    Page<BookLoan> loans1 = bookLoanRepository.findByStatusAndUser(BookLoanStatus.CHECKED_OUT, user, PageRequest.of(0, 20));
                    Page<BookLoan> loans2 = bookLoanRepository.findByStatusAndUser(BookLoanStatus.OVERDUE, user, PageRequest.of(0, 20));
                    
                    List<BookLoan> activeLoans = new ArrayList<>();
                    activeLoans.addAll(loans1.getContent());
                    activeLoans.addAll(loans2.getContent());

                    if(activeLoans.isEmpty()) {
                        databaseContext = "The user is currently NOT borrowing any books.";
                    } else {
                        StringBuilder sb = new StringBuilder("The user is currently borrowing:\n");
                        for (BookLoan bl : activeLoans) {
                            sb.append("- ").append(bl.getBook().getTitle())
                              .append(" (Checkout date: ").append(bl.getCheckOutDate())
                              .append(", Due date: ").append(bl.getDueDate())
                              .append(", Status: ").append(bl.getStatus()).append(")\n");
                        }
                        databaseContext = sb.toString();
                    }
                } else {
                    databaseContext = "User is not logged in or cannot be identified."; // Should not happen with secured endpoint
                }
                break;
            case "MY_HISTORY":
                if (user != null) {
                    long totalRead = bookLoanRepository.countReturnedBooksByUserId(user.getId());
                    databaseContext = "The user has successfully read and returned a total of " + totalRead + " books in their history.";
                } else {
                    databaseContext = "User is not logged in.";
                }
                break;
            case "TOP_BOOKS":
                Page<Book> topBooks = bookRepository.getMostReadBooks(PageRequest.of(0, 5));
                if (topBooks.isEmpty()) {
                    databaseContext = "We don't have enough data to determine the top books right now.";
                } else {
                    databaseContext = "Here are the top most read/popular books in the system:\n" + formatBooks(topBooks);
                }
                break;
            case "GENERAL":
            default:
                databaseContext = "System is a library management app. Be polite and helpful. If they ask for recommendations, tell them we have: Clean Code, Harry Potter, The Great Gatsby.";
                break;
        }

        // Step 3: Natural Language Generation
        String nlgPrompt = "You are a friendly, helpful Library Assistant for UTCBook. " +
                "The user says: \"" + userMessage + "\"\n" +
                "You queried the database and got this contextual data:\n" +
                "[" + databaseContext + "]\n" +
                "Now, answer the user naturally in Vietnamese. Make it conversational, engaging. " +
                "CRITICAL RULE: If the contextual data states 'No books exactly matched' or 'No specific keyword provided', you MUST explicitly tell the user that we couldn't find books exactly matching their request. Do NOT lie or pretend that the fallback 'popular/latest books' belong to the genre/topic the user asked for. Instead, politely introduce them as alternatives. " +
                "Do not use markdown formatting, just natural text.";

        String finalReply = callGemini(nlgPrompt, false);

        return ChatbotResponse.builder().reply(finalReply).build();
    }

    private String callGemini(String prompt, boolean jsonFormat) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (jsonFormat) {
            prompt += " IMPORTANT: Please output ONLY valid JSON format without markdown blocks.";
        }

        JsonObject requestBody = new JsonObject();

        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("text", prompt);
        parts.add(textObj);
        contentObj.add("parts", parts);
        contents.add(contentObj);

        requestBody.add("contents", contents);

        // Optional: Ensure it responds in raw JSON format using system configuration if needed, but the prompt above usually suffices for Gemini.
        if(jsonFormat) {
            JsonObject genConfig = new JsonObject();
            genConfig.addProperty("responseMimeType", "application/json");
            requestBody.add("generationConfig", genConfig);
        } else {
             JsonObject genConfig = new JsonObject();
             requestBody.add("generationConfig", genConfig);
        }

        String targetUrl = geminiApiUrl + "?key=" + geminiApiKey;
        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(requestBody), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, entity, String.class);
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            
            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates returned from Gemini");
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            if (!firstCandidate.has("content")) {
                if (firstCandidate.has("finishReason") && "SAFETY".equals(firstCandidate.get("finishReason").getAsString())) {
                   return "Xin lỗi, câu hỏi của bạn có chứa nội dung không phù hợp với chuẩn mực an toàn. Vui lòng hỏi lại với từ ngữ khác nhé!";
                }
                throw new RuntimeException("Missing content in Gemini response candidate");
            }

            String responseText = firstCandidate
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
                    
            // Sometimes Gemini wraps JSON in ```json\n \n``` despite instructions. We can clean it up
            if (jsonFormat) {
                responseText = responseText.replace("```json", "").replace("```", "").trim();
            }
            return responseText;
        } catch (Exception e) {
            log.error("Error communicating with Gemini", e);
            if (jsonFormat) {
                return "{\"intent\": \"GENERAL\", \"keyword\": null}";
            }
            
            String errMsg = e.getMessage();
            if (errMsg != null && (errMsg.contains("503") || errMsg.contains("high demand") || errMsg.contains("UNAVAILABLE"))) {
                return "Hệ thống AI của Google hiện đang tạm thời quá tải do lưu lượng truy cập cao. Bạn vui lòng đợi vài phút rồi chat lại nhé!";
            }
            if (errMsg != null && errMsg.contains("429")) {
                return "Phần xử lý AI đã hết hạn mức truy cập tạm thời. Vui lòng nghỉ tay ít phút rồi truy cập lại!";
            }

            return "Lỗi Server/Mô hình AI: " + e.getMessage();
        }
    }

    private String formatBooks(Page<Book> books) {
        if(books == null || books.isEmpty()) return "No books available in the system right now.";
        StringBuilder sb = new StringBuilder();
        for(Book b : books.getContent()) {
            String genre = b.getGenre() != null ? b.getGenre().getName() : "General";
            sb.append("- ").append(b.getTitle()).append(" by ").append(b.getAuthor())
              .append(" (Genre: ").append(genre)
              .append(", Total Reads: ").append(b.getBorrowCount() != null ? b.getBorrowCount() : 0)
              .append(", Available: ").append(b.getAvailableCopies()).append(")\n");
        }
        return sb.toString();
    }
}
