package get2gether.exception;

import get2gether.model.ResourceType;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(ResourceType type, String message) {
    super("Resource of type " + type + " not found with " + message);
  }
}
