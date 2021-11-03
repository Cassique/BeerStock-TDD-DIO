package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerEmptyStockException extends Exception {

    public BeerEmptyStockException() {
        super(String.format("The Stock is Empty %s: %s"));
    }
}