package com.codecrafthub.controller;

import com.codecrafthub.model.Course;
import com.codecrafthub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for tracking courses you want to learn. Every method here just
 * translates an HTTP request into a call on {@link CourseService} - all the
 * real logic (validation, file storage) lives there.
 *
 * <pre>
 * POST   /api/courses       - add a new course
 * GET    /api/courses       - get all courses
 * GET    /api/courses/{id}  - get a specific course
 * PUT    /api/courses/{id}  - update a course
 * DELETE /api/courses/{id}  - delete a course
 * </pre>
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    // Spring automatically "injects" the CourseService bean here - we don't
    // create it ourselves with "new".
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** POST /api/courses - Add a new course. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 Created on success
    public Course addCourse(@Valid @RequestBody Course course) {
        return courseService.createCourse(course);
    }

    /** GET /api/courses - Get all courses. */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    /** GET /api/courses/{id} - Get a specific course. */
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Integer id) {
        return courseService.getCourseById(id);
    }

    /** PUT /api/courses/{id} - Update a course. */
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Integer id, @Valid @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    /** DELETE /api/courses/{id} - Delete a course. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // -----------------------------------------------------------------
    // Error handling - these methods run automatically whenever one of
    // the endpoints above throws the matching exception type. They turn
    // Java exceptions into clean, predictable JSON error responses.
    // -----------------------------------------------------------------

    /**
     * Triggered by @Valid when a required field (name, description,
     * target_date, status) is missing or blank in the request body.
     * Responds 400 Bad Request with one message per invalid field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMissingFields(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Triggered by CourseService for "course not found" (404), "invalid
     * status value" (400), and file read/write errors (500).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleServiceErrors(ResponseStatusException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /**
     * Triggered when the request body isn't valid JSON, or a field has the
     * wrong type/format (e.g. target_date isn't "yyyy-MM-dd").
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Malformed request body. Check field types and date format (yyyy-MM-dd).");
        return ResponseEntity.badRequest().body(body);
    }
}
