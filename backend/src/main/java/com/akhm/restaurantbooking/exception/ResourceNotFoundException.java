package com.akhm.restaurantbooking.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
       super(message);
    }
}