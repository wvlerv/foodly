package com.foodly.backend.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.foodly.backend.entity.User;
import com.foodly.backend.repository.UserRepository;
import com.foodly.backend.security.JwtAuthenticationFilter;
import com.foodly.backend.security.TokenBlacklistService;
import com.foodly.backend.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggingAndPiiTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(JwtAuthenticationFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }
    @Test
    void testLoggingLevel_WhenUnexpectedException_ThenLogsErrorLevel() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer valid.token.here");
        when(blacklistService.isBlacklisted(anyString())).thenThrow(new RuntimeException("Redis cluster unavailable"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        List<ILoggingEvent> logsList = listAppender.list;

        ILoggingEvent errorLog = logsList.stream()
                .filter(event -> event.getFormattedMessage().contains("LOG-07"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected security log 'LOG-07' was missing"));

        assertEquals(Level.ERROR, errorLog.getLevel(),
                "CRITICAL: Unhandled security filter pipeline failures must drop severe ERROR frames!");
    }

    @Test
    void testPiiProtection_RawTokensAndCleartextEmailsAreNotLogged() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String brokenJwtPayload = "Bearer completelyMalformedTokenDataWithoutDotNotation";
        request.addHeader("Authorization", brokenJwtPayload);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty(), "No audit trace events logged during pipeline validation.");

        for (ILoggingEvent logEvent : logsList) {
            String message = logEvent.getFormattedMessage();

            assertFalse(message.contains("completelyMalformedTokenDataWithoutDotNotation"),
                    "SECURITY BREACH: Raw incoming crypt-token payload found explicitly dumped inside log data!");

            if (message.contains("LOG-08")) {
                assertTrue(message.contains("String length:"),
                        "COMPLIANCE FAILURE: LOG-08 must strip token contents and output character dimensions exclusively.");
            }
        }
    }
    @Test
    void testLogFormat_ContainsRequiredFields() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String cleartextEmail = "banned.user@gmail.com";
        request.addHeader("Authorization", "Bearer mock.jwt.token");

        when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.getEmailFromToken(anyString())).thenReturn(cleartextEmail);

        User bannedUser = new User();
        bannedUser.setEmail(cleartextEmail);
        bannedUser.setBanned(true);
        when(userRepository.findByEmail(cleartextEmail)).thenReturn(Optional.of(bannedUser));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        List<ILoggingEvent> logsList = listAppender.list;
        ILoggingEvent sampleLog = logsList.stream()
                .filter(event -> event.getFormattedMessage().contains("LOG-09"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected execution hook 'LOG-09' failed to fire"));

        assertNotNull(sampleLog.getLevel(), "Log structure mismatch: Missing operational 'level' flag context.");

        assertTrue(sampleLog.getTimeStamp() > 0, "Log structure mismatch: Missing a valid system timestamp entry.");

        String message = sampleLog.getFormattedMessage();
        assertFalse(message.contains(cleartextEmail), "PII LEAK: Cleartext email detected inside banned warning message.");
        assertTrue(message.contains("ba***@gmail.com"), "ANONYMIZER ERROR: Identity mask pattern failure on LOG-09 payload.");
    }
}