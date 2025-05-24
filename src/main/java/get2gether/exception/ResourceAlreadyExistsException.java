package get2gether.exception;

import get2gether.enums.ResourceType;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(ResourceType resourceType, String message) {
        super(String.format("%s already exists with %s", resourceType, message));
    }
}
