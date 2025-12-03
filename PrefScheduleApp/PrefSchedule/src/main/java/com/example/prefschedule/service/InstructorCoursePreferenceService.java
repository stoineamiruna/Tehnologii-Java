package com.example.prefschedule.service;

import com.example.prefschedule.dto.*;
import com.example.prefschedule.entity.Course;
import com.example.prefschedule.entity.InstructorCoursePreference;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.repository.CourseRepository;
import com.example.prefschedule.repository.InstructorCoursePreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorCoursePreferenceService {

    private final InstructorCoursePreferenceRepository preferenceRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public InstructorPreferencesResponse setPreferencesForCourse(InstructorPreferencesRequest request) {
        log.info("Setting preferences for course ID: {}", request.getCourseId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        double totalWeight = request.getPreferences().stream()
                .mapToDouble(CompulsoryCourseWeightDTO::getWeightPercentage)
                .sum();

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            log.warn("Total weight is {}, not 100%", totalWeight);
        }

        preferenceRepository.deleteByCourseId(course.getId());
        preferenceRepository.flush();

        List<InstructorCoursePreference> preferences = request.getPreferences().stream()
                .map(pref -> InstructorCoursePreference.builder()
                        .course(course)
                        .compulsoryCourseAbbr(pref.getCompulsoryCourseAbbr())
                        .weightPercentage(pref.getWeightPercentage())
                        .build())
                .collect(Collectors.toList());

        preferenceRepository.saveAll(preferences);

        return convertToResponse(course, preferences);
    }

    @Transactional(readOnly = true)
    public InstructorPreferencesResponse getPreferencesForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        List<InstructorCoursePreference> preferences = preferenceRepository.findByCourseId(courseId);

        return convertToResponse(course, preferences);
    }

    @Transactional(readOnly = true)
    public List<InstructorPreferencesResponse> getPreferencesForInstructor(Long instructorId) {
        List<InstructorCoursePreference> preferences = preferenceRepository.findByInstructorId(instructorId);

        Map<Course, List<InstructorCoursePreference>> groupedByCourse = preferences.stream()
                .collect(Collectors.groupingBy(InstructorCoursePreference::getCourse));

        return groupedByCourse.entrySet().stream()
                .map(entry -> convertToResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstructorCoursePreferenceDTO> getAllPreferences() {
        return preferenceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePreferencesForCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        preferenceRepository.deleteByCourseId(courseId);
        log.info("Deleted preferences for course ID: {}", courseId);
    }

    private InstructorPreferencesResponse convertToResponse(Course course,
                                                            List<InstructorCoursePreference> preferences) {
        Map<String, Double> gradeWeights = preferences.stream()
                .collect(Collectors.toMap(
                        InstructorCoursePreference::getCompulsoryCourseAbbr,
                        InstructorCoursePreference::getWeightPercentage
                ));

        return InstructorPreferencesResponse.builder()
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseName(course.getName())
                .gradeWeights(gradeWeights)
                .build();
    }

    private InstructorCoursePreferenceDTO convertToDTO(InstructorCoursePreference preference) {
        return InstructorCoursePreferenceDTO.builder()
                .id(preference.getId())
                .courseId(preference.getCourse().getId())
                .compulsoryCourseAbbr(preference.getCompulsoryCourseAbbr())
                .weightPercentage(preference.getWeightPercentage())
                .build();
    }
}
