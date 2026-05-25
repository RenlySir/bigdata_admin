package com.bigdata.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * File Validation Utility
 * Validates file types, MIME types, and magic numbers
 */
@Slf4j
public class FileValidationUtil {

    // Allowed file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv", "xlsx", "xls", "json");

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "text/csv",
        "application/csv",
        "text/plain",  // Some CSV files are detected as text/plain
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/json"
    );

    // Magic numbers for file type validation
    private static final Set<String> CSV_MAGIC_NUMBERS = Set.of(
        new String(new byte[]{0x50, 0x4B, 0x03, 0x04})  // ZIP (XLSX)
    );

    // Excel magic numbers
    private static final byte[] XLSX_MAGIC = {0x50, 0x4B, 0x03, 0x04};  // ZIP header
    private static final byte[] XLS_MAGIC = {0xD0, 0xCF, 0x11, 0xE0};   // OLE2 header

    /**
     * Validate file for import operations
     * @param file File to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        // Check file size (max 100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 100MB");
        }

        // Get filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Sanitize filename
        String sanitizedFilename = sanitizeFilename(filename);
        if (!sanitizedFilename.equals(filename)) {
            log.warn("Filename was sanitized: {} -> {}", filename, sanitizedFilename);
        }

        // Check file extension
        String extension = getFileExtension(sanitizedFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            // Allow files with non-standard MIME types if they have valid extensions
            // This handles cases where CSV files are detected as "text/plain"
            if (!extension.equals("csv") && !extension.equals("json")) {
                throw new IllegalArgumentException(
                    "Invalid file content type. Allowed types: " + String.join(", ", ALLOWED_MIME_TYPES)
                );
            }
        }

        // Validate magic numbers for binary files
        if (extension.equals("xlsx") || extension.equals("xls")) {
            if (!validateExcelMagicNumber(file)) {
                throw new IllegalArgumentException("File content does not match its extension");
            }
        }

        log.debug("File validation passed: {} ({} bytes, {})", sanitizedFilename, file.getSize(), contentType);
    }

    /**
     * Sanitize filename to prevent path traversal and other attacks
     * @param filename Original filename
     * @return Sanitized filename
     */
    private static String sanitizeFilename(String filename) {
        // Remove any path components
        String sanitized = filename.substring(Math.max(0, filename.lastIndexOf('/') + 1));
        sanitized = sanitized.substring(Math.max(0, sanitized.lastIndexOf('\\') + 1));

        // Remove null bytes
        sanitized = sanitized.replace("\0", "");

        // Limit filename length
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, 255 - extension.length() - 1) + "." + extension;
        }

        return sanitized;
    }

    /**
     * Get file extension from filename
     * @param filename Filename
     * @return File extension (without dot)
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * Validate Excel file magic number
     * @param file File to validate
     * @return true if valid Excel file, false otherwise
     */
    private static boolean validateExcelMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4) {
                return false;
            }

            // Check for XLSX (ZIP format)
            if (header[0] == XLSX_MAGIC[0] && header[1] == XLSX_MAGIC[1] &&
                header[2] == XLSX_MAGIC[2] && header[3] == XLSX_MAGIC[3]) {
                return true;
            }

            // Check for XLS (OLE2 format)
            if (header[0] == XLS_MAGIC[0] && header[1] == XLS_MAGIC[1] &&
                header[2] == XLS_MAGIC[2] && header[3] == XLS_MAGIC[3]) {
                return true;
            }

            return false;
        } catch (IOException e) {
            log.error("Error reading file header for validation", e);
            return false;
        }
    }

    /**
     * Check if file type is allowed
     * @param extension File extension
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowedExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
}
