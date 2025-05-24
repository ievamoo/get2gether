package get2gether.exception;

import get2gether.enums.ResourceType;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(ResourceType resourceType, String message) {
        super(String.format("%s not found with %s", resourceType, message));
    }
}
