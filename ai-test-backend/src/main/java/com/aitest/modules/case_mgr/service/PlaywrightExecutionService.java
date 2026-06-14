package com.aitest.modules.case_mgr.service;

import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.aitest.modules.case_suite.mapper.TestExecutionDetailMapper;
import com.aitest.modules.case_suite.mapper.TestExecutionMapper;
import com.aitest.modules.llm.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service that generates Playwright code via LLM and executes it in headed Chrome.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaywrightExecutionService {

    private final LlmService llmService;
    private final TestExecutionMapper testExecutionMapper;
    private final TestExecutionDetailMapper testExecutionDetailMapper;

    @Value("${playwright.project-dir:#{null}}")
    private String customProjectDir;

    /**
     * Execute a test case asynchronously:
     * 1. Generate Playwright code via LLM
     * 2. Write to temp file
     * 3. Run Playwright CLI in headed mode
     * 4. Update execution record
     */
    public CompletableFuture<TestExecution> executeAsync(TestCase testCase, TestExecution execution) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return doExecute(testCase, execution);
            } catch (Exception e) {
                log.error("Execution failed for case {}: {}", testCase.getCaseCode(), e.getMessage(), e);
                execution.setStatus("FAILED");
                execution.setFailedCount(1);
                execution.setFinishedAt(LocalDateTime.now());
                testExecutionMapper.updateById(execution);
                return execution;
            }
        });
    }

    private TestExecution doExecute(TestCase testCase, TestExecution execution) {
        log.info("Starting execution for case: {} ({})", testCase.getCaseCode(), testCase.getCaseName());

        // Step 1: Generate Playwright code via LLM
        String naturalSteps = testCase.getNaturalLanguageSteps();
        if (naturalSteps == null || naturalSteps.isBlank()) {
            naturalSteps = testCase.getCaseName(); // fallback to case name
        }

        log.info("Calling LLM to generate Playwright code...");
        String playwrightCode = llmService.generatePlaywrightCode(
                testCase.getCaseName(), naturalSteps, testCase.getPreconditions());

        if (playwrightCode.startsWith("[LLM")) {
            // LLM call failed
            TestExecutionDetail detail = createDetail(execution.getId(), testCase.getId(), "FAILED");
            detail.setErrorMessage(playwrightCode);
            detail.setExecutionLog("LLM code generation failed:\n" + playwrightCode);
            detail.setFinishedAt(LocalDateTime.now());
            testExecutionDetailMapper.insert(detail);

            execution.setStatus("FAILED");
            execution.setFailedCount(1);
            execution.setFinishedAt(LocalDateTime.now());
            testExecutionMapper.updateById(execution);
            return execution;
        }

        // Store generated code in execution
        log.info("Generated Playwright code ({} chars)", playwrightCode.length());

        // Step 2: Write to temp file in Playwright project
        Path projectDir = getPlaywrightProjectDir();
        String fileName = "generated-" + testCase.getCaseCode() + "-" + System.currentTimeMillis() + ".spec.ts";
        Path specFile = projectDir.resolve("tests").resolve(fileName);

        try {
            Files.writeString(specFile, playwrightCode, StandardCharsets.UTF_8);
            log.info("Written spec file: {}", specFile);
        } catch (Exception e) {
            log.error("Failed to write spec file: {}", e.getMessage());
            execution.setStatus("FAILED");
            execution.setFailedCount(1);
            execution.setFinishedAt(LocalDateTime.now());
            testExecutionMapper.updateById(execution);
            return execution;
        }

        // Step 3: Run Playwright CLI in headed mode
        TestExecutionDetail detail = createDetail(execution.getId(), testCase.getId(), "RUNNING");
        detail.setStartedAt(LocalDateTime.now());
        testExecutionDetailMapper.insert(detail);

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        int exitCode = -1;

        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                // Windows: use cmd.exe /c npx
                pb = new ProcessBuilder(
                        "cmd.exe", "/c", "npx", "playwright", "test", fileName,
                        "--headed",
                        "--project=chromium",
                        "--reporter=list"
                );
            } else {
                pb = new ProcessBuilder(
                        "npx", "playwright", "test", fileName,
                        "--headed",
                        "--project=chromium",
                        "--reporter=list"
                );
            }
            pb.directory(projectDir.toFile());
            pb.environment().put("CI", ""); // Ensure non-CI mode
            pb.redirectErrorStream(false);

            Process process = pb.start();

            // Read stdout and stderr concurrently
            Thread stdoutThread = readStream(process, stdout, false);
            Thread stderrThread = readStream(process, stderr, true);

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            stdoutThread.join(5000);
            stderrThread.join(5000);

            if (!finished) {
                process.destroyForcibly();
                detail.setStatus("FAILED");
                detail.setErrorMessage("Execution timed out (120s)");
            } else {
                exitCode = process.exitValue();
                detail.setStatus(exitCode == 0 ? "SUCCESS" : "FAILED");
            }
        } catch (Exception e) {
            log.error("Playwright execution error: {}", e.getMessage());
            detail.setStatus("FAILED");
            detail.setErrorMessage("Process error: " + e.getMessage());
        }

        // Step 4: Update records
        detail.setFinishedAt(LocalDateTime.now());
        detail.setDurationMs(java.time.Duration.between(detail.getStartedAt(), detail.getFinishedAt()).toMillis());
        detail.setExecutionLog(stdout.toString());
        if (!stderr.isEmpty()) {
            detail.setErrorMessage((detail.getErrorMessage() != null ? detail.getErrorMessage() + "\n" : "") + stderr.toString());
        }
        testExecutionDetailMapper.updateById(detail);

        execution.setStatus(detail.getStatus());
        execution.setFinishedAt(detail.getFinishedAt());
        if ("SUCCESS".equals(detail.getStatus())) {
            execution.setPassedCount(1);
        } else {
            execution.setFailedCount(1);
        }
        testExecutionMapper.updateById(execution);

        // Step 5: Clean up temp file
        try {
            Files.deleteIfExists(specFile);
            log.info("Cleaned up spec file: {}", specFile);
        } catch (Exception e) {
            log.warn("Failed to clean up spec file: {}", e.getMessage());
        }

        log.info("Execution completed for case {}: {} (exitCode={})", testCase.getCaseCode(), detail.getStatus(), exitCode);
        return execution;
    }

    private TestExecutionDetail createDetail(Long executionId, Long caseId, String status) {
        TestExecutionDetail detail = new TestExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setCaseId(caseId);
        detail.setStatus(status);
        detail.setStartedAt(LocalDateTime.now());
        return detail;
    }

    private Thread readStream(Process process, StringBuilder buffer, boolean isError) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(isError ? process.getErrorStream() : process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
            } catch (Exception e) {
                buffer.append("[Read error: ").append(e.getMessage()).append("]");
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private Path getPlaywrightProjectDir() {
        if (customProjectDir != null && !customProjectDir.isBlank()) {
            return Path.of(customProjectDir);
        }
        // Default: sibling directory of backend
        Path current = Path.of(System.getProperty("user.dir"));
        // If running from backend dir, go up one level
        if (current.getFileName().toString().equals("ai-test-backend")) {
            return current.getParent().resolve("ai-test-playwright");
        }
        return current.resolve("ai-test-playwright");
    }
}
