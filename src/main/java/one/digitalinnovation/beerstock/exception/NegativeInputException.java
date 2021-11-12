package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NegativeInputException extends Exception{
    public NegativeInputException(int quantity) {
    super(String.format(" Exceeds the min stock capacity: %s",quantity));
}
}