package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repositories.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public ActivityResponse trackActivity(ActivityRequest activityRequest){

        boolean isValidUser = userValidationService.validateUser(activityRequest.getUserId());

        if(!isValidUser){
            throw new RuntimeException("Invalid User: "+activityRequest.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(activityRequest.getUserId())
                .type(activityRequest.getType())
                .caloriesBurned(activityRequest.getCaloriesBurned())
                .duration(activityRequest.getDuration())
                .startTime(activityRequest.getStartTime())
                .endTime(activityRequest.getEndTime())
                .additionalMetrics(activityRequest.getAdditionalMetrics())
                .build();

        Activity activitySaved = activityRepository.save(activity);

        //Publish to rabbitmq for AI processing
        try{
            rabbitTemplate.convertAndSend(exchange, routingKey, activitySaved);
        }catch (Exception e){
            log.error("Failed to publish activity to RabbitMQ!: {}", e.getMessage());
        }

        return mapToResponse(activitySaved);
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        return activityRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ActivityResponse getActivity(String activityId){
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(()-> new RuntimeException("Activity not found"));
    }

    private ActivityResponse mapToResponse(Activity activity){
        ActivityResponse activityResponse = new ActivityResponse();
        activityResponse.setId(activity.getId());
        activityResponse.setUserId(activity.getUserId());
        activityResponse.setType(activity.getType());
        activityResponse.setCaloriesBurned(activity.getCaloriesBurned());
        activityResponse.setDuration(activity.getDuration());
        activityResponse.setStartTime(activity.getStartTime());
        activityResponse.setEndTime(activity.getEndTime());
        activityResponse.setAdditionalMetrics(activity.getAdditionalMetrics());
        activityResponse.setCreatedAt(activity.getCreatedAt());
        activityResponse.setUpdatedAt(activity.getUpdatedAt());

        return activityResponse;
    }
}
