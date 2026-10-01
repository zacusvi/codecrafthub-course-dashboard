package com.codecrafthub.service;

import com.codecrafthub.model.Course;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * All business logic AND file storage for courses live here, in one place,
 * which keeps things simple for a learning project:
 *
 *   - CRUD operations (create, read, update, delete)
 *   - Validation rules that plain Bean Validation (@NotBlank/@NotNull)
 *     cannot express, such as "status must be one of these 3 exact strings"
 *   - Reading/writing the whole course list to "courses.json" on every change
 *
 * There is NO database involved. "courses.json" is our entire data store.
 */
@Service
public class CourseService {

    // The only 3 status values we accept. Using a Set makes the "is this
    // value allowed?" check a single, fast lookup.
    private static final Set<String> VALID_STATUSES = Set.of("Not Started", "In Progress", "Completed");

    // Path to the JSON file, configurable via application.properties
    // (data.file.path). Defaults to "courses.json" in the app's working directory.
    private final String filePath;

    // Jackson's ObjectMapper does the actual JSON <-> Java conversion.
    private final ObjectMapper objectMapper;

    public CourseService(@Value("${data.file.path:courses.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        // Lets Jackson understand java.time types like LocalDate/LocalDateTime.
        this.objectMapper.registerModule(new JavaTimeModule());
        // Write dates as "2026-12-31" strings instead of numeric timestamps.
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Makes the JSON file human-readable (pretty-printed) when we save it.
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Runs once, right after Spring creates this service. Guarantees
     * "courses.json" exists (requirement: auto-create the file if missing)
     * so the very first GET/POST never fails with a "file not found" error.
     */
    @PostConstruct
    public void initializeDataFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                // Start with an empty JSON array - an empty course list.
                objectMapper.writeValue(file, new ArrayList<Course>());
            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not create data file '" + filePath + "': " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------------
    // CRUD operations
    // ---------------------------------------------------------------------

    /** GET /api/courses */
    public synchronized List<Course> getAllCourses() {
        return readFromFile();
    }

    /** GET /api/courses/{id} */
    public synchronized Course getCourseById(Integer id) {
        return readFromFile().stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> courseNotFound(id));
    }

    /** POST /api/courses */
    public synchronized Course createCourse(Course newCourse) {
        validateStatus(newCourse.getStatus());

        List<Course> courses = readFromFile();

        // Simple auto-increment starting at 1: one more than the current
        // highest id (or 1 if the list is empty).
        int nextId = courses.stream()
                .mapToInt(Course::getId)
                .max()
                .orElse(0) + 1;

        newCourse.setId(nextId);
        newCourse.setCreatedAt(LocalDateTime.now());

        courses.add(newCourse);
        writeToFile(courses);
        return newCourse;
    }

    /** PUT /api/courses/{id} */
    public synchronized Course updateCourse(Integer id, Course updatedCourse) {
        validateStatus(updatedCourse.getStatus());

        List<Course> courses = readFromFile();

        for (int i = 0; i < courses.size(); i++) {
            Course existing = courses.get(i);
            if (existing.getId().equals(id)) {
                // Keep the original id and created_at - only the editable
                // fields are replaced by the request body.
                updatedCourse.setId(existing.getId());
                updatedCourse.setCreatedAt(existing.getCreatedAt());
                courses.set(i, updatedCourse);
                writeToFile(courses);
                return updatedCourse;
            }
        }

        throw courseNotFound(id);
    }

    /** DELETE /api/courses/{id} */
    public synchronized void deleteCourse(Integer id) {
        List<Course> courses = readFromFile();
        boolean removed = courses.removeIf(course -> course.getId().equals(id));
        if (!removed) {
            throw courseNotFound(id);
        }
        writeToFile(courses);
    }

    // ---------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------

    private void validateStatus(String status) {
        if (status != null && !VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Must be exactly one of: " + VALID_STATUSES);
        }
    }

    private ResponseStatusException courseNotFound(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + id);
    }

    // ---------------------------------------------------------------------
    // File storage (reads/writes the whole courses.json file every time)
    // ---------------------------------------------------------------------

    private List<Course> readFromFile() {
        try {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            Course[] courses = objectMapper.readValue(file, Course[].class);
            return new ArrayList<>(List.of(courses));
        } catch (IOException e) {
            // File read error -> surfaced as a clean 500 response instead of
            // crashing the request with a raw stack trace.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read courses from '" + filePath + "': " + e.getMessage());
        }
    }

    private void writeToFile(List<Course> courses) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(file, courses);
        } catch (IOException e) {
            // File write error -> surfaced as a clean 500 response instead of
            // crashing the request with a raw stack trace.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to write courses to '" + filePath + "': " + e.getMessage());
        }
    }
}
